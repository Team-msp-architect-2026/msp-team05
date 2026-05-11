# 프론트엔드 S3 버킷
resource "aws_s3_bucket" "frontend" {
  bucket        = "${var.project_name}-${var.environment}-frontend"
  force_destroy = true

  tags = {
    Name        = "${var.project_name}-${var.environment}-frontend"
    Project     = var.project_name
    Environment = var.environment
  }
}

# 프론트엔드 버킷 버전 관리
resource "aws_s3_bucket_versioning" "frontend" {
  bucket = aws_s3_bucket.frontend.id
  versioning_configuration {
    status = "Enabled"
  }
}

# 프론트엔드 버킷 퍼블릭 액세스 차단
resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket                  = aws_s3_bucket.frontend.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# WAF 로그 S3 버킷 (이름이 반드시 aws-waf-logs- 로 시작해야 함)
resource "aws_s3_bucket" "waf_logs" {
  bucket        = "aws-waf-logs-${var.project_name}-${var.environment}"
  force_destroy = true

  tags = {
    Name        = "aws-waf-logs-${var.project_name}-${var.environment}"
    Project     = var.project_name
    Environment = var.environment
  }
}

# WAF 로그 버킷 버전 관리
resource "aws_s3_bucket_versioning" "waf_logs" {
  bucket = aws_s3_bucket.waf_logs.id
  versioning_configuration {
    status = "Enabled"
  }
}

# WAF 로그 버킷 퍼블릭 액세스 차단 비활성화 (CloudFront 로그용 ACL 필요)
resource "aws_s3_bucket_public_access_block" "waf_logs" {
  bucket                  = aws_s3_bucket.waf_logs.id
  block_public_acls       = false
  block_public_policy     = true
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# WAF 로그 버킷 ACL 활성화 (CloudFront 로그용)
resource "aws_s3_bucket_ownership_controls" "waf_logs" {
  bucket = aws_s3_bucket.waf_logs.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "waf_logs" {
  depends_on = [
    aws_s3_bucket_ownership_controls.waf_logs,
    aws_s3_bucket_public_access_block.waf_logs
  ]
  bucket = aws_s3_bucket.waf_logs.id
  acl    = "log-delivery-write"
}