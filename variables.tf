variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "eu-west-2"
}

variable "project_name" {
  description = "프로젝트 이름"
  type        = string
  default     = "kky"
}

variable "environment" {
  description = "환경"
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "퍼블릭 서브넷 CIDR 목록"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_was_cidrs" {
  description = "프라이빗 서브넷 WAS CIDR 목록"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.20.0/24"]
}

variable "private_subnet_db_cidrs" {
  description = "프라이빗 서브넷 DB CIDR 목록"
  type        = list(string)
  default     = ["10.0.100.0/24", "10.0.200.0/24"]
}

variable "availability_zones" {
  description = "가용 영역 목록"
  type        = list(string)
  default     = ["eu-west-2a", "eu-west-2c"]
}

variable "ami_id" {
  description = "EC2 AMI ID"
  type        = string
}

variable "certificate_arn" {
  description = "ACM certificate ARN for HTTPS"
  type        = string
  default     = ""
}

variable "desired_capacity" {
  description = "Auto Scaling 원하는 용량"
  type        = number
  default     = 2
}

variable "domain_name" {
  description = "메인 도메인 이름"
  type        = string
  default     = "kkybot.click"
}

variable "db_password" {
  description = "RDS 마스터 비밀번호"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT Secret Key"
  type        = string
  sensitive   = true
}

variable "cloudfront_secret" {
  description = "CloudFront → ALB 인증용 시크릿 헤더 값"
  type        = string
  sensitive   = true
}

variable "slack_webhook_url" {
  description = "Slack Incoming Webhook URL"
  type        = string
  sensitive   = true
}