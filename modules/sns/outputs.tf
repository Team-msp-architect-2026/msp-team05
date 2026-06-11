output "sns_topic_arn" {
  description = "SNS Topic ARN (CloudWatch Alarm alarm_actions에 연결)"
  value       = aws_sns_topic.waf_alarm.arn
}

output "sns_topic_name" {
  description = "SNS Topic Name"
  value       = aws_sns_topic.waf_alarm.name
}