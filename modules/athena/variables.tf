variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment"
  type        = string
}

variable "waf_logs_bucket_id" {
  description = "WAF 로그 S3 버킷 ID"
  type        = string
}

variable "waf_logs_bucket_arn" {
  description = "WAF 로그 S3 버킷 ARN"
  type        = string
}