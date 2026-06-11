output "dashboard_name" {
  description = "CloudWatch 대시보드 이름"
  value       = aws_cloudwatch_dashboard.main.dashboard_name
}
output "sns_topic_arn" {
  description = "알람 발송용 SNS Topic ARN"
  value       = aws_sns_topic.alarm.arn
}

output "dashboard_name" {
  description = "CloudWatch 대시보드 이름"
  value       = aws_cloudwatch_dashboard.main.dashboard_name
}