output "alb_sg_id" {
  description = "ALB Security Group ID"
  value       = aws_security_group.alb.id
}

output "ec2_sg_id" {
  description = "EC2 Security Group ID"
  value       = aws_security_group.ec2.id
}

output "rds_sg_id" {
  description = "RDS Security Group ID"
  value       = aws_security_group.rds.id
}

output "redis_sg_id" {
  description = "Redis Security Group ID"
  value       = aws_security_group.redis.id
}