variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnet IDs for ALB"
  type        = list(string)
}

variable "private_was_subnet_ids" {
  description = "Private WAS subnet IDs for EC2"
  type        = list(string)
}

variable "alb_sg_id" {
  description = "ALB Security Group ID"
  type        = string
}

variable "ec2_sg_id" {
  description = "EC2 Security Group ID"
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t2.micro"
}

variable "ami_id" {
  description = "AMI ID for EC2"
  type        = string
}

variable "min_size" {
  description = "Auto Scaling minimum size"
  type        = number
  default     = 1
}

variable "max_size" {
  description = "Auto Scaling maximum size"
  type        = number
  default     = 3
}

variable "desired_capacity" {
  description = "Auto Scaling desired capacity"
  type        = number
  default     = 1
}

variable "cpu_target_value" {
  description = "Auto Scaling CPU target value"
  type        = number
  default     = 70
}

variable "certificate_arn" {
  description = "ACM certificate ARN for HTTPS"
  type        = string
  default     = ""
}

variable "cloudfront_secret" {
  description = "CloudFront Secret Header 값"
  type        = string
  sensitive   = true
}