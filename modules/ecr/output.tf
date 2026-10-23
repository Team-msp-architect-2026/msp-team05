output "repository_url" {
  description = "ECR Repository URL"
  value       = aws_ecr_repository.backend.repository_url
}

output "repository_name" {
  description = "ECR Repository Name"
  value       = aws_ecr_repository.backend.name
}