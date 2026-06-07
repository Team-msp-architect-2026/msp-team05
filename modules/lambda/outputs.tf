output "lambda_arn" {
  description = "Lambda 함수 ARN (SNS 구독 대상)"
  value       = aws_lambda_function.waf_auto_response.arn
}

output "lambda_function_name" {
  description = "Lambda 함수 이름 (Lambda Permission 설정용)"
  value       = aws_lambda_function.waf_auto_response.function_name
}

output "lambda_log_group_name" {
  description = "CloudWatch 로그 그룹 이름 (E2E 테스트 로그 확인용)"
  value       = aws_cloudwatch_log_group.lambda_logs.name
}