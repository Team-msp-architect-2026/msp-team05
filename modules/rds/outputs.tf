output "rds_endpoint" {
  description = "RDS endpoint"
  value       = aws_db_instance.main.endpoint
}

output "rds_host" {
  description = "RDS host (포트 제외)"
  value       = aws_db_instance.main.address
}

output "rds_port" {
  description = "RDS port"
  value       = aws_db_instance.main.port
}

output "rds_db_name" {
  description = "RDS database name"
  value       = aws_db_instance.main.db_name
}

output "rds_username" {
  description = "RDS master username (이슈 7번 Secrets Manager 저장용)"
  value       = aws_db_instance.main.username
}

output "rds_arn" {
  description = "RDS ARN"
  value       = aws_db_instance.main.arn
}

output "rds_instance_id" {
  description = "RDS 인스턴스 ID (CloudWatch용)"
  value       = aws_db_instance.main.identifier
}