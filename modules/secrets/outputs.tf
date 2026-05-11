output "db_secret_arn" {
  description = "DB 접속 정보 Secrets Manager ARN"
  value       = aws_secretsmanager_secret.db.arn
}

output "jwt_secret_arn" {
  description = "JWT Secret Key Secrets Manager ARN"
  value       = aws_secretsmanager_secret.jwt.arn
}

output "redis_secret_arn" {
  description = "Redis 접속 정보 Secrets Manager ARN"
  value       = aws_secretsmanager_secret.redis.arn
}