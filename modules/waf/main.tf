# WAF Web ACL (CloudFront용 - us-east-1)
resource "aws_wafv2_web_acl" "main" {
  provider    = aws.us_east_1
  name        = "${var.project_name}-${var.environment}-waf"
  description = "KKYBot WAF Bot Control"
  scope       = "CLOUDFRONT"

  default_action {
    allow {}
  }

  # 규칙 1 - 해외 IP 차단 (우선순위 0)
  rule {
    name     = "GeoBlockRule"
    priority = 0

    action {
      block {}
    }

    statement {
      not_statement {
        statement {
          geo_match_statement {
            country_codes = ["KR"]
          }
        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "GeoBlockRule"
      sampled_requests_enabled   = true
    }
  }

  # 규칙 2 - WAF Bot Control Targeted + JA4 핑거프린팅 (우선순위 1)
  rule {
    name     = "BotControlRule"
    priority = 1

    override_action {
      none {}
    }

    statement {
      managed_rule_group_statement {
        name        = "AWSManagedRulesBotControlRuleSet"
        vendor_name = "AWS"

        managed_rule_group_configs {
          aws_managed_rules_bot_control_rule_set {
            inspection_level = "TARGETED"
          }
        }

        # JA4 핑거프린팅 - IP 로테이션 우회 봇 탐지
        rule_action_override {
          name = "TGT_TokenReuseIp"
          action_to_use {
            captcha {}
          }
        }

        rule_action_override {
          name = "TGT_VolumetricSession"
          action_to_use {
            captcha {}
          }
        }

        scope_down_statement {
          or_statement {
            statement {
              byte_match_statement {
                search_string         = "/api/queue"
                positional_constraint = "STARTS_WITH"
                field_to_match {
                  uri_path {}
                }
                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
            statement {
              byte_match_statement {
                search_string         = "/api/reservations"
                positional_constraint = "STARTS_WITH"
                field_to_match {
                  uri_path {}
                }
                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
          }
        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "BotControlRule"
      sampled_requests_enabled   = true
    }
  }

  # 규칙 3 - Rate Based Rule (우선순위 2)
  rule {
    name     = "RateBasedRule"
    priority = 2

    action {
      block {}
    }

    statement {
      rate_based_statement {
        limit              = 1000
        aggregate_key_type = "IP"

        scope_down_statement {
          or_statement {
            statement {
              byte_match_statement {
                search_string         = "/api/queue"
                positional_constraint = "STARTS_WITH"
                field_to_match {
                  uri_path {}
                }
                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
            statement {
              byte_match_statement {
                search_string         = "/api/reservations"
                positional_constraint = "STARTS_WITH"
                field_to_match {
                  uri_path {}
                }
                text_transformation {
                  priority = 0
                  type     = "NONE"
                }
              }
            }
          }
        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "RateBasedRule"
      sampled_requests_enabled   = true
    }
  }

  # 규칙 4 - 헬스체크 예외 처리 (우선순위 3)
  rule {
    name     = "HealthCheckExceptionRule"
    priority = 3

    action {
      allow {}
    }

    statement {
      byte_match_statement {
        search_string         = "/actuator/health"
        positional_constraint = "EXACTLY"
        field_to_match {
          uri_path {}
        }
        text_transformation {
          priority = 0
          type     = "NONE"
        }
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "HealthCheckExceptionRule"
      sampled_requests_enabled   = true
    }
  }

  # CAPTCHA 설정
  captcha_config {
    immunity_time_property {
      immunity_time = 300
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "${var.project_name}-${var.environment}-waf"
    sampled_requests_enabled   = true
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-waf"
    Project     = var.project_name
    Environment = var.environment
  }
}

# WAF 로그 설정 - S3 저장
resource "aws_wafv2_web_acl_logging_configuration" "main" {
  provider                = aws.us_east_1
  log_destination_configs = [var.waf_logs_bucket_arn]
  resource_arn            = aws_wafv2_web_acl.main.arn
}