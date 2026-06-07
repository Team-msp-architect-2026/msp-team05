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