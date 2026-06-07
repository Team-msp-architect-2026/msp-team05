# SNS Topic - 알람 발송용
resource "aws_sns_topic" "alarm" {
  name = "${var.project_name}-${var.environment}-alarm"

  tags = {
    Name        = "${var.project_name}-${var.environment}-alarm"
    Project     = var.project_name
    Environment = var.environment
  }
}

# ─────────────────────────────────────────
# WAF 알람
# ─────────────────────────────────────────

resource "aws_cloudwatch_metric_alarm" "waf_blocked" {
  alarm_name          = "${var.project_name}-${var.environment}-waf-blocked"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "BlockedRequests"
  namespace           = "AWS/WAFV2"
  period              = 60
  statistic           = "Sum"
  threshold           = 100
  alarm_description   = "WAF 차단 요청 1분간 100건 초과"
  alarm_actions       = [aws_sns_topic.alarm.arn]

  dimensions = {
    WebACL = var.waf_web_acl_name
    Region = "us-east-1"
    Rule   = "ALL"
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# ─────────────────────────────────────────
# ALB 알람
# ─────────────────────────────────────────

resource "aws_cloudwatch_metric_alarm" "alb_5xx" {
  alarm_name          = "${var.project_name}-${var.environment}-alb-5xx"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "HTTPCode_ELB_5XX_Count"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Sum"
  threshold           = 10
  alarm_description   = "ALB 5XX 에러 1분간 10건 초과"
  alarm_actions       = [aws_sns_topic.alarm.arn]

  dimensions = {
    LoadBalancer = var.alb_arn_suffix
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_cloudwatch_metric_alarm" "alb_response_time" {
  alarm_name          = "${var.project_name}-${var.environment}-alb-response-time"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "TargetResponseTime"
  namespace           = "AWS/ApplicationELB"
  period              = 60
  statistic           = "Average"
  threshold           = 2
  alarm_description   = "ALB 평균 응답시간 2초 초과"
  alarm_actions       = [aws_sns_topic.alarm.arn]

  dimensions = {
    LoadBalancer = var.alb_arn_suffix
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# ─────────────────────────────────────────
# EC2 알람
# ─────────────────────────────────────────

resource "aws_cloudwatch_metric_alarm" "ec2_cpu" {
  alarm_name          = "${var.project_name}-${var.environment}-ec2-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Average"
  threshold           = 70
  alarm_description   = "EC2 CPU 사용률 70% 초과"
  alarm_actions       = [aws_sns_topic.alarm.arn]

  dimensions = {
    AutoScalingGroupName = var.asg_name
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# ─────────────────────────────────────────
# ASG 알람
# ─────────────────────────────────────────

resource "aws_cloudwatch_metric_alarm" "asg_max_capacity" {
  alarm_name          = "${var.project_name}-${var.environment}-asg-max-capacity"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "GroupInServiceInstances"
  namespace           = "AWS/AutoScaling"
  period              = 60
  statistic           = "Average"
  threshold           = 3
  alarm_description   = "ASG 인스턴스 수 최대치 도달"
  alarm_actions       = [aws_sns_topic.alarm.arn]

  dimensions = {
    AutoScalingGroupName = var.asg_name
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# ─────────────────────────────────────────
# RDS 알람
# ─────────────────────────────────────────

resource "aws_cloudwatch_metric_alarm" "rds_connections" {
  alarm_name          = "${var.project_name}-${var.environment}-rds-connections"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = 60
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDS 커넥션 수 80개 초과"
  alarm_actions       = [aws_sns_topic.alarm.arn]

  dimensions = {
    DBInstanceIdentifier = var.rds_instance_id
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  alarm_name          = "${var.project_name}-${var.environment}-rds-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 60
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDS CPU 사용률 80% 초과"
  alarm_actions       = [aws_sns_topic.alarm.arn]

  dimensions = {
    DBInstanceIdentifier = var.rds_instance_id
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# ─────────────────────────────────────────
# CloudWatch 대시보드
# ─────────────────────────────────────────

resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.project_name}-${var.environment}-dashboard"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 12
        height = 6
        properties = {
          title   = "WAF 차단 요청"
          region  = "us-east-1"
          metrics = [["AWS/WAFV2", "BlockedRequests", "WebACL", var.waf_web_acl_name, "Region", "us-east-1", "Rule", "ALL"]]
          period  = 60
          stat    = "Sum"
          view    = "timeSeries"
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 12
        height = 6
        properties = {
          title   = "ALB 5XX 에러"
          region  = "eu-west-2"
          metrics = [["AWS/ApplicationELB", "HTTPCode_ELB_5XX_Count", "LoadBalancer", var.alb_arn_suffix]]
          period  = 60
          stat    = "Sum"
          view    = "timeSeries"
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          title   = "EC2 CPU 사용률"
          region  = "eu-west-2"
          metrics = [["AWS/EC2", "CPUUtilization", "AutoScalingGroupName", var.asg_name]]
          period  = 60
          stat    = "Average"
          view    = "timeSeries"
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          title   = "ASG 인스턴스 수"
          region  = "eu-west-2"
          metrics = [["AWS/AutoScaling", "GroupInServiceInstances", "AutoScalingGroupName", var.asg_name]]
          period  = 60
          stat    = "Average"
          view    = "timeSeries"
        }
      },
      {
        type   = "metric"
        x      = 0
        y      = 12
        width  = 12
        height = 6
        properties = {
          title   = "RDS 커넥션 수"
          region  = "eu-west-2"
          metrics = [["AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", var.rds_instance_id]]
          period  = 60
          stat    = "Average"
          view    = "timeSeries"
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 12
        width  = 12
        height = 6
        properties = {
          title   = "ALB 응답시간"
          region  = "eu-west-2"
          metrics = [["AWS/ApplicationELB", "TargetResponseTime", "LoadBalancer", var.alb_arn_suffix]]
          period  = 60
          stat    = "Average"
          view    = "timeSeries"
        }
      }
    ]
  })
}