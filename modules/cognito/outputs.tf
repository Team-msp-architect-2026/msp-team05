output "user_pool_id" {
  description = "Cognito User Pool ID"
  value       = aws_cognito_user_pool.main.id
}

output "user_pool_arn" {
  description = "Cognito User Pool ARN"
  value       = aws_cognito_user_pool.main.arn
}

output "app_client_id" {
  description = "Cognito App Client ID"
  value       = aws_cognito_user_pool_client.main.id
}

output "cognito_endpoint" {
  description = "Cognito 엔드포인트"
  value       = "https://cognito-idp.${var.environment == "prod" ? "eu-west-2" : "eu-west-2"}.amazonaws.com/${aws_cognito_user_pool.main.id}"
}