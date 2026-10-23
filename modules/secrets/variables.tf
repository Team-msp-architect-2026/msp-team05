variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment"
  type        = string
}

variable "db_username" {
  description = "RDS 마스터 유저명"
  type        = string
  default     = "kkybot_admin"
}

variable "db_password" {
  description = "RDS 마스터 비밀번호"
  type        = string
  sensitive   = true
}

variable "rds_endpoint" {
  description = "RDS 엔드포인트"
  type        = string
}

variable "rds_db_name" {
  description = "RDS 데이터베이스 이름"
  type        = string
  default     = "kkybot"
}

variable "redis_endpoint" {
  description = "Redis 엔드포인트"
  type        = string
}

variable "jwt_secret" {
  description = "JWT Secret Key"
  type        = string
  sensitive   = true
}