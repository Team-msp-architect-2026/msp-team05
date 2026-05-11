output "alb_arn" {
  description = "ALB ARN"
  value       = aws_lb.app.arn
}

output "alb_dns_name" {
  description = "ALB DNS Name"
  value       = aws_lb.app.dns_name
}

output "target_group_arn" {
  description = "Target Group ARN"
  value       = aws_lb_target_group.app.arn
}

output "autoscaling_group_name" {
  description = "Auto Scaling Group Name"
  value       = aws_autoscaling_group.app.name
}

output "ec2_iam_role_arn" {
  description = "EC2 IAM Role ARN"
  value       = aws_iam_role.ec2.arn
}