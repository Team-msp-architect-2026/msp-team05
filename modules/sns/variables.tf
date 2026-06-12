variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment"
  type        = string
}

variable "lambda_arn" {
  description = "Lambda 함수 ARN (SNS 구독 대상)"
  type        = string
}

variable "lambda_function_name" {
  description = "Lambda 함수 이름 (invoke permission 설정용)"
  type        = string
}