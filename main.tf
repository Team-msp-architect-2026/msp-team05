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
  certificate_arn        = var.certificate_arn
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

# Cognito 모듈
module "cognito" {
  source = "./modules/cognito"

  project_name = var.project_name
  environment  = var.environment
}

# AWS Budgets - $50 알림
resource "aws_budgets_budget" "main" {
  name         = "${var.project_name}-${var.environment}-budget"
  budget_type  = "COST"
  limit_amount = "50"
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 100
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = ["wnsvy0128@gmail.com"]
  }
}