terraform {
  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 5.0"
      configuration_aliases = [aws.us_east_1]
    }
  }
}

# WAF 알람 수신용 SNS 토픽
resource "aws_sns_topic" "waf_alarm" {
  name = "${var.project_name}-${var.environment}-waf-alarm"

  tags = {
    Name        = "${var.project_name}-${var.environment}-waf-alarm"
    Project     = var.project_name
    Environment = var.environment
  }
}

# CloudWatch가 이 토픽에 publish할 수 있도록 허용
resource "aws_sns_topic_policy" "cloudwatch_publish" {
  arn = aws_sns_topic.waf_alarm.arn

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudWatchPublish"
        Effect = "Allow"
        Principal = {
          Service = "cloudwatch.amazonaws.com"
        }
        Action   = "SNS:Publish"
        Resource = aws_sns_topic.waf_alarm.arn
      }
    ]
  })
}

# SNS → Lambda 구독 연결
resource "aws_sns_topic_subscription" "lambda" {
  topic_arn = aws_sns_topic.waf_alarm.arn
  protocol  = "lambda"
  endpoint  = var.lambda_arn
}

# SNS가 Lambda를 invoke할 수 있도록 허용
resource "aws_lambda_permission" "sns_invoke" {
  provider      = aws.us_east_1
  statement_id  = "AllowSNSInvoke"
  action        = "lambda:InvokeFunction"
  function_name = var.lambda_function_name
  principal     = "sns.amazonaws.com"
  source_arn    = aws_sns_topic.waf_alarm.arn
}