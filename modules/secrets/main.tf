# Secrets Manager - DB 접속 정보
resource "aws_secretsmanager_secret" "db" {
  name                    = "${var.project_name}-${var.environment}-db-secret"
  description             = "KKYBot RDS MySQL 접속 정보"
  recovery_window_in_days = 0

  tags = {
    Name        = "${var.project_name}-${var.environment}-db-secret"
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    host     = var.rds_endpoint
    port     = 3306
    dbname   = var.rds_db_name
    username = var.db_username
    password = var.db_password
  })
}

# Secrets Manager - JWT Secret Key
resource "aws_secretsmanager_secret" "jwt" {
  name                    = "${var.project_name}-${var.environment}-jwt-secret"
  description             = "KKYBot JWT Secret Key"
  recovery_window_in_days = 0

  tags = {
    Name        = "${var.project_name}-${var.environment}-jwt-secret"
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id = aws_secretsmanager_secret.jwt.id
  secret_string = jsonencode({
    secret = var.jwt_secret
  })
}

# Secrets Manager - Redis 접속 정보
resource "aws_secretsmanager_secret" "redis" {
  name                    = "${var.project_name}-${var.environment}-redis-secret"
  description             = "KKYBot Redis 접속 정보"
  recovery_window_in_days = 0

  tags = {
    Name        = "${var.project_name}-${var.environment}-redis-secret"
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_secretsmanager_secret_version" "redis" {
  secret_id = aws_secretsmanager_secret.redis.id
  secret_string = jsonencode({
    host = var.redis_endpoint
    port = 6379
  })
}