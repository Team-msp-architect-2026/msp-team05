output "athena_database_name" {
  description = "Athena 데이터베이스 이름"
  value       = aws_athena_database.waf_logs.name
}

output "athena_workgroup_name" {
  description = "Athena 워크그룹 이름"
  value       = aws_athena_workgroup.main.name
}