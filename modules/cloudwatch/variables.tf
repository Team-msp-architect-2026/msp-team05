terraform {
  required_providers {
    aws = {
      source                = "hashicorp/aws"
      version               = "~> 5.0"
      configuration_aliases = [aws.us_east_1]
    }
  }
}

variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경"
  type        = string
}

variable "sns_topic_arn" {
  description = "알람 발송용 SNS Topic ARN"
  type        = string
}

variable "sns_topic_arn_us_east_1" {
  description = "알람 발송용 SNS Topic ARN (us-east-1, CloudFront WAF 알람 전용)"
  type        = string
}

variable "waf_web_acl_name" {
  description = "WAF Web ACL 이름"
  type        = string
}

variable "alb_arn_suffix" {
  description = "ALB ARN suffix"
  type        = string
}

variable "target_group_arn_suffix" {
  description = "Target Group ARN suffix"
  type        = string
}

variable "asg_name" {
  description = "Auto Scaling Group 이름"
  type        = string
}

variable "rds_instance_id" {
  description = "RDS 인스턴스 ID"
  type        = string
}