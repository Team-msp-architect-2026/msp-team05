terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# 기본 provider (런던)
provider "aws" {
  region = var.aws_region
}

# CloudFront + WAF용 provider (버지니아 - AWS 규칙)
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"
}

module "vpc" {
  source = "./modules/vpc"

  project_name             = var.project_name
  environment              = var.environment
  vpc_cidr                 = var.vpc_cidr
  public_subnet_cidrs      = var.public_subnet_cidrs
  private_subnet_was_cidrs = var.private_subnet_was_cidrs
  private_subnet_db_cidrs  = var.private_subnet_db_cidrs
  availability_zones       = var.availability_zones
}

module "security" {
  source = "./modules/security"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.vpc.vpc_id
}

module "ec2" {
  source = "./modules/ec2"

  project_name           = var.project_name
  environment            = var.environment
  vpc_id                 = module.vpc.vpc_id
  public_subnet_ids      = module.vpc.public_subnet_ids
  private_was_subnet_ids = module.vpc.private_was_subnet_ids
  alb_sg_id              = module.security.alb_sg_id
  ec2_sg_id              = module.security.ec2_sg_id
  ami_id                 = var.ami_id
  certificate_arn        = module.acm.alb_certificate_arn
  desired_capacity       = var.desired_capacity
  cloudfront_secret      = var.cloudfront_secret
}

module "rds" {
  source = "./modules/rds"

  project_name          = var.project_name
  environment           = var.environment
  private_db_subnet_ids = module.vpc.private_db_subnet_ids
  rds_sg_id             = module.security.rds_sg_id
  db_password           = var.db_password
}

module "redis" {
  source = "./modules/redis"

  project_name          = var.project_name
  environment           = var.environment
  private_db_subnet_ids = module.vpc.private_db_subnet_ids
  redis_sg_id           = module.security.redis_sg_id
}

# S3 모듈 (프론트엔드 + WAF 로그 버킷)
module "s3" {
  source = "./modules/s3"

  project_name = var.project_name
  environment  = var.environment
}

# WAF 모듈 (us-east-1)
module "waf" {
  source = "./modules/waf"
  providers = {
    aws.us_east_1 = aws.us_east_1
  }

  project_name        = var.project_name
  environment         = var.environment
  waf_logs_bucket_arn = module.s3.waf_logs_bucket_arn
}

# CloudFront 모듈
module "cloudfront" {
  source = "./modules/cloudfront"
  providers = {
    aws.us_east_1 = aws.us_east_1
  }

  project_name                = var.project_name
  environment                 = var.environment
  frontend_bucket_id          = module.s3.frontend_bucket_id
  frontend_bucket_arn         = module.s3.frontend_bucket_arn
  frontend_bucket_domain_name = module.s3.frontend_bucket_domain_name
  alb_dns_name                = module.ec2.alb_dns_name
  waf_acl_arn                 = module.waf.waf_acl_arn
  waf_logs_bucket_id          = module.s3.waf_logs_bucket_id
  certificate_arn             = module.acm.cloudfront_certificate_arn
  domain_name                 = var.domain_name
  cloudfront_secret           = var.cloudfront_secret
}

# Route53 모듈
module "route53" {
  source = "./modules/route53"

  project_name              = var.project_name
  environment               = var.environment
  domain_name               = var.domain_name
  cloudfront_domain_name    = module.cloudfront.cloudfront_domain_name
  cloudfront_hosted_zone_id = module.cloudfront.cloudfront_hosted_zone_id
  alb_dns_name              = module.ec2.alb_dns_name
  alb_hosted_zone_id        = module.ec2.alb_hosted_zone_id
}

# ACM 모듈
module "acm" {
  source = "./modules/acm"
  providers = {
    aws.us_east_1 = aws.us_east_1
  }

  project_name = var.project_name
  environment  = var.environment
  domain_name  = var.domain_name
  zone_id      = module.route53.zone_id
}

# Athena 모듈
module "athena" {
  source = "./modules/athena"

  project_name        = var.project_name
  environment         = var.environment
  waf_logs_bucket_id  = module.s3.waf_logs_bucket_id
  waf_logs_bucket_arn = module.s3.waf_logs_bucket_arn
}

# Secrets Manager 모듈
module "secrets" {
  source = "./modules/secrets"

  project_name   = var.project_name
  environment    = var.environment
  db_password    = var.db_password
  rds_endpoint   = module.rds.rds_host
  rds_db_name    = module.rds.rds_db_name
  redis_endpoint = module.redis.redis_endpoint
  jwt_secret     = var.jwt_secret
}

# CloudWatch 모듈
module "cloudwatch" {
  source = "./modules/cloudwatch"

  project_name            = var.project_name
  environment             = var.environment
  sns_topic_arn           = module.sns.sns_topic_arn
  waf_web_acl_name        = module.waf.waf_acl_name
  alb_arn_suffix          = module.ec2.alb_arn_suffix
  target_group_arn_suffix = module.ec2.target_group_arn_suffix
  asg_name                = module.ec2.autoscaling_group_name
  rds_instance_id         = module.rds.rds_instance_id
}

# Lambda 모듈
module "lambda" {
  source = "./modules/lambda"
  providers = {
    aws.us_east_1 = aws.us_east_1
  }

  project_name      = var.project_name
  environment       = var.environment
  waf_acl_id        = module.waf.waf_acl_id
  waf_acl_arn       = module.waf.waf_acl_arn
  waf_acl_name      = module.waf.waf_acl_name
  slack_webhook_url = var.slack_webhook_url
}

# SNS 모듈
module "sns" {
  source = "./modules/sns"
  providers = {
    aws.us_east_1 = aws.us_east_1
  }

  project_name         = var.project_name
  environment          = var.environment
  lambda_arn           = module.lambda.lambda_arn
  lambda_function_name = module.lambda.lambda_function_name
}

# ECR 모듈
module "ecr" {
  source       = "./modules/ecr"
  project_name = var.project_name
  environment  = var.environment
}

# Cognito 모듈 - 현재 미사용
# module "cognito" {
#   source = "./modules/cognito"
#
#   project_name = var.project_name
#   environment  = var.environment
# }

# AWS Budgets - 교육 계정 권한 없음으로 주석처리
# resource "aws_budgets_budget" "main" {
#   name         = "${var.project_name}-${var.environment}-budget"
#   budget_type  = "COST"
#   limit_amount = "50"
#   limit_unit   = "USD"
#   time_unit    = "MONTHLY"
#
#   notification {
#     comparison_operator        = "GREATER_THAN"
#     threshold                  = 100
#     threshold_type             = "PERCENTAGE"
#     notification_type          = "ACTUAL"
#     subscriber_email_addresses = ["wnsvy0128@gmail.com"]
#   }
# }