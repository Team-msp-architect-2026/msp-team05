variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment"
  type        = string
}

variable "waf_acl_id" {
  description = "WAF Web ACL ID"
  type        = string
}

variable "waf_acl_arn" {
  description = "WAF Web ACL ARN"
  type        = string
}

variable "waf_acl_name" {
  description = "WAF Web ACL Name"
  type        = string
}

variable "slack_webhook_url" {
  description = "Slack Incoming Webhook URL"
  type        = string
  sensitive   = true
}