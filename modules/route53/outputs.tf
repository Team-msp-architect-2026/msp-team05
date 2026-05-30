output "zone_id" {
  description = "Route53 호스팅 존 ID"
  value       = aws_route53_zone.main.zone_id
}

output "name_servers" {
  description = "Route53 네임서버 목록"
  value       = aws_route53_zone.main.name_servers
}