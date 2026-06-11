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
      timestamp                    BIGINT,
      formatversion                INT,
      webaclid                     STRING,
      terminatingruleid            STRING,
      terminatingruletype          STRING,
      action                       STRING,
      terminatingrulematchdetails  ARRAY<STRING>,
      httpsourcename               STRING,
      httpsourceid                 STRING,
      rulegrouplist                ARRAY<STRING>,
      ratebasedrulelist            ARRAY<STRING>,
      nonterminatingmatchingrules  ARRAY<STRING>,
      requestheadersinserted       STRING,
      responsecodesent             STRING,
      httprequest STRUCT<
        clientip:    STRING,
        country:     STRING,
        headers:     ARRAY<STRUCT<name:STRING, value:STRING>>,
        uri:         STRING,
        args:        STRING,
        httpversion: STRING,
        httpmethod:  STRING,
        requestid:   STRING,
        fragment:    STRING,
        scheme:      STRING,
        host:        STRING
      >,
      labels ARRAY<STRUCT<name:STRING>>,
      ja3fingerprint STRING,
      ja4fingerprint STRING
    )
    ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
    WITH SERDEPROPERTIES ('ignore.malformed.json' = 'true')
    STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'
    OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
    LOCATION 's3://${var.waf_logs_bucket_id}/AWSLogs/611058323802/WAFLogs/cloudfront/kky-prod-waf/'
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
      COUNT(*) AS count,
      ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) AS percentage
    FROM waf_logs
    WHERE from_unixtime(timestamp / 1000) >= current_timestamp - interval '1' hour
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
      COUNT(*) AS count
    FROM waf_logs
    WHERE action = 'BLOCK'
      AND from_unixtime(timestamp / 1000) >= current_timestamp - interval '1' hour
    GROUP BY action, terminatingruleid
    ORDER BY count DESC;
  EOT
}

# URI별 차단 현황 쿼리
resource "aws_athena_named_query" "blocked_by_uri" {
  name      = "${var.project_name}-${var.environment}-blocked-by-uri"
  workgroup = aws_athena_workgroup.main.id
  database  = aws_athena_database.waf_logs.name

  query = <<-EOT
    SELECT
      httprequest.uri AS uri,
      COUNT(*) AS blocked_count
    FROM waf_logs
    WHERE action = 'BLOCK'
    GROUP BY httprequest.uri
    ORDER BY blocked_count DESC
    LIMIT 10;
  EOT
}

# IP별 차단 현황 쿼리
resource "aws_athena_named_query" "blocked_by_ip" {
  name      = "${var.project_name}-${var.environment}-blocked-by-ip"
  workgroup = aws_athena_workgroup.main.id
  database  = aws_athena_database.waf_logs.name

  query = <<-EOT
    SELECT
      httprequest.clientip AS client_ip,
      httprequest.country  AS country,
      COUNT(*) AS blocked_count
    FROM waf_logs
    WHERE action = 'BLOCK'
    GROUP BY httprequest.clientip, httprequest.country
    ORDER BY blocked_count DESC
    LIMIT 10;
  EOT
}