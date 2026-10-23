output "frontend_bucket_id" {
  description = "프론트엔드 S3 버킷 ID"
  value       = aws_s3_bucket.frontend.id
}

output "frontend_bucket_arn" {
  description = "프론트엔드 S3 버킷 ARN"
  value       = aws_s3_bucket.frontend.arn
}

output "frontend_bucket_domain_name" {
  description = "프론트엔드 S3 버킷 도메인"
  value       = aws_s3_bucket.frontend.bucket_regional_domain_name
}

output "waf_logs_bucket_id" {
  description = "WAF 로그 S3 버킷 ID"
  value       = aws_s3_bucket.waf_logs.id
}

output "waf_logs_bucket_arn" {
  description = "WAF 로그 S3 버킷 ARN"
  value       = aws_s3_bucket.waf_logs.arn
}