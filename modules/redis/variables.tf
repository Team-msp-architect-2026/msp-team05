variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment"
  type        = string
}

variable "private_db_subnet_ids" {
  description = "Private DB subnet IDs"
  type        = list(string)
}

variable "redis_sg_id" {
  description = "Redis Security Group ID"
  type        = string
}

variable "node_type" {
  description = "ElastiCache node type"
  type        = string
  default     = "cache.t3.micro"
}

variable "snapshot_retention_limit" {
  description = "백업 보존 기간 (일)"
  type        = number
  default     = 3
}