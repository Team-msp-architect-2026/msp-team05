output "cloudfront_certificate_arn" {
  description = "CloudFront용 ACM 인증서 ARN"
  value       = aws_acm_certificate_validation.cloudfront.certificate_arn
}

output "alb_certificate_arn" {
  description = "ALB용 ACM 인증서 ARN"
  value       = aws_acm_certificate_validation.alb.certificate_arn
}