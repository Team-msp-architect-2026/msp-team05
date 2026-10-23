output "sns_topic_arn" {
  description = "알람 발송용 SNS Topic ARN"
  value       = var.sns_topic_arn
}

output "dashboard_name" {
  description = "CloudWatch 대시보드 이름"
  value       = aws_cloudwatch_dashboard.main.dashboard_name
}