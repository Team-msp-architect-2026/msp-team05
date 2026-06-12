terraform {
  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 5.0"
      configuration_aliases = [aws.us_east_1]
    }
    archive = {
      source  = "hashicorp/archive"
      version = "~> 2.0"
    }
  }
}

# Lambda 실행 IAM 역할
resource "aws_iam_role" "lambda_exec" {
  name = "${var.project_name}-${var.environment}-waf-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-${var.environment}-waf-lambda-role"
    Project     = var.project_name
    Environment = var.environment
  }
}

# WAFv2 조회·수정 + CloudWatch Logs 기록 권한
resource "aws_iam_role_policy" "lambda_waf" {
  name = "${var.project_name}-${var.environment}-lambda-waf-policy"
  role = aws_iam_role.lambda_exec.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "wafv2:GetWebACL",
          "wafv2:UpdateWebACL"
        ]
        Resource = [
          var.waf_acl_arn,
          "arn:aws:wafv2:us-east-1:611058323802:global/managedruleset/*/*"
        ]
        Resource = var.waf_acl_arn
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = "arn:aws:logs:*:*:*"
      }
    ]
  })
}

# function/ 디렉터리를 zip으로 패키징
data "archive_file" "lambda_zip" {
  type        = "zip"
  source_dir  = "${path.module}/function"
  output_path = "${path.module}/function.zip"
}

# WAF 자동 대응 Lambda 함수
resource "aws_lambda_function" "waf_auto_response" {
  provider         = aws.us_east_1
  filename         = data.archive_file.lambda_zip.output_path
  function_name    = "${var.project_name}-${var.environment}-waf-auto-response"
  role             = aws_iam_role.lambda_exec.arn
  handler          = "waf_auto_response.lambda_handler"
  runtime          = "python3.12"
  source_code_hash = data.archive_file.lambda_zip.output_base64sha256
  timeout          = 30

  environment {
    variables = {
      WAF_ACL_ID   = var.waf_acl_id
      WAF_ACL_ARN  = var.waf_acl_arn
      WAF_ACL_NAME = var.waf_acl_name
      WAF_SCOPE    = "CLOUDFRONT"
      WAF_REGION   = "us-east-1"
      WAF_ACL_ID        = var.waf_acl_id
      WAF_ACL_ARN       = var.waf_acl_arn
      WAF_ACL_NAME      = var.waf_acl_name
      WAF_SCOPE         = "CLOUDFRONT"
      WAF_REGION        = "us-east-1"
      SLACK_WEBHOOK_URL = var.slack_webhook_url
    }
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-waf-auto-response"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Lambda 실행 로그 그룹
resource "aws_cloudwatch_log_group" "lambda_logs" {
  provider          = aws.us_east_1
  name              = "/aws/lambda/${aws_lambda_function.waf_auto_response.function_name}"
  retention_in_days = 30

  tags = {
    Name        = "${var.project_name}-${var.environment}-waf-lambda-logs"
    Project     = var.project_name
    Environment = var.environment
  }
}