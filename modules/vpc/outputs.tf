output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "퍼블릭 서브넷 ID 목록"
  value       = aws_subnet.public[*].id
}

output "private_was_subnet_ids" {
  description = "프라이빗 WAS 서브넷 ID 목록"
  value       = aws_subnet.private_was[*].id
}

output "private_db_subnet_ids" {
  description = "프라이빗 DB 서브넷 ID 목록"
  value       = aws_subnet.private_db[*].id
}

output "nat_gateway_ids" {
  description = "NAT Gateway ID 목록"
  value       = aws_nat_gateway.main[*].id
}