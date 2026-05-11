# Athena 데이터베이스
resource "aws_athena_database" "waf_logs" {
  name          = "${var.project_name}_${var.environment}_waf_logs"
  bucket        = var.waf_logs_bucket_id
  force_destroy = true
}

# Athena 워크그룹
resource "aws_athena_workgroup" "main" {
  name          = "${var.project_name}-${var.environment}-waf-workgroup"
  force_destroy = true

  configuration {
    enforce_workgroup_configuration    = true
    publish_cloudwatch_metrics_enabled = true

    result_configuration {
      output_location = "s3://${var.waf_logs_bucket_id}/athena-results/"
    }
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-waf-workgroup"
    Project     = var.project_name
    Environment = var.environment
  }
}

# WAF 로그 테이블 생성
resource "aws_athena_named_query" "create_waf_table" {
  name      = "${var.project_name}-${var.environment}-create-waf-table"
  workgroup = aws_athena_workgroup.main.id
  database  = aws_athena_database.waf_logs.name

  query = <<-EOT
    CREATE EXTERNAL TABLE IF NOT EXISTS waf_logs (
      timestamp BIGINT,
      formatversion INT,
      webaclid STRING,
      terminatingruleid STRING,
      terminatingruletype STRING,
      action STRING,
      httpsourcename STRING,
      httpsourceid STRING,
      httprequest STRING
    )
    ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
    STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'
    OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
    LOCATION 's3://${var.waf_logs_bucket_id}/AWSLogs/'
    TBLPROPERTIES ('has_encrypted_data'='false');
  EOT
}

# 탐지율 분석 쿼리
resource "aws_athena_named_query" "detection_rate" {
  name      = "${var.project_name}-${var.environment}-detection-rate"
  workgroup = aws_athena_workgroup.main.id
  database  = aws_athena_database.waf_logs.name

  query = <<-EOT
    SELECT
      action,
      COUNT(*) as count,
      ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
    FROM waf_logs
    WHERE from_unixtime(timestamp/1000) >= current_timestamp - interval '1' hour
    GROUP BY action
    ORDER BY count DESC;
  EOT
}

# 오탐률 분석 쿼리
resource "aws_athena_named_query" "false_positive_rate" {
  name      = "${var.project_name}-${var.environment}-false-positive-rate"
  workgroup = aws_athena_workgroup.main.id
  database  = aws_athena_database.waf_logs.name

  query = <<-EOT
    SELECT
      action,
      terminatingruleid,
      COUNT(*) as count
    FROM waf_logs
    WHERE action = 'BLOCK'
      AND from_unixtime(timestamp/1000) >= current_timestamp - interval '1' hour
    GROUP BY action, terminatingruleid
    ORDER BY count DESC;
  EOT
}