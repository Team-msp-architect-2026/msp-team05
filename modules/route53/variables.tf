variable "project_name" {
  description = "프로젝트 이름"
  type        = string
}

variable "environment" {
  description = "환경"
  type        = string
}

variable "domain_name" {
  description = "메인 도메인 이름"
  type        = string
}

variable "cloudfront_domain_name" {
  description = "CloudFront 도메인 이름"
  type        = string
}

variable "cloudfront_hosted_zone_id" {
  description = "CloudFront 호스팅 존 ID"
  type        = string
}

variable "alb_dns_name" {
  description = "ALB DNS 이름"
  type        = string
}

variable "alb_hosted_zone_id" {
  description = "ALB 호스팅 존 ID"
  type        = string
}