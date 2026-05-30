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
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment"
  type        = string
}

variable "frontend_bucket_id" {
  description = "프론트엔드 S3 버킷 ID"
  type        = string
}

variable "frontend_bucket_arn" {
  description = "프론트엔드 S3 버킷 ARN"
  type        = string
}

variable "frontend_bucket_domain_name" {
  description = "프론트엔드 S3 버킷 도메인"
  type        = string
}

variable "alb_dns_name" {
  description = "ALB DNS Name"
  type        = string
}

variable "waf_acl_arn" {
  description = "WAF Web ACL ARN"
  type        = string
}

variable "waf_logs_bucket_id" {
  description = "WAF 로그 S3 버킷 ID (CloudFront 접근 로그용)"
  type        = string
}

variable "certificate_arn" {
  description = "ACM 인증서 ARN (CloudFront용 us-east-1)"
  type        = string
}

variable "domain_name" {
  description = "메인 도메인 이름"
  type        = string
}