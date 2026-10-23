# DB Subnet Group
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = var.private_db_subnet_ids

  tags = {
    Name        = "${var.project_name}-${var.environment}-db-subnet-group"
    Project     = var.project_name
    Environment = var.environment
  }
}

# RDS Parameter Group (utf8mb4 한글 지원)
resource "aws_db_parameter_group" "main" {
  name   = "${var.project_name}-${var.environment}-mysql8"
  family = "mysql8.0"

  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }

  parameter {
    name  = "collation_server"
    value = "utf8mb4_unicode_ci"
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-mysql8"
    Project     = var.project_name
    Environment = var.environment
  }
}

# RDS MySQL
resource "aws_db_instance" "main" {
  identifier        = "${var.project_name}-${var.environment}-mysql"
  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = var.instance_class
  allocated_storage = var.allocated_storage
  storage_type      = "gp2"

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [var.rds_sg_id]
  parameter_group_name   = aws_db_parameter_group.main.name

  multi_az                = var.multi_az
  publicly_accessible     = false
  skip_final_snapshot     = true
  backup_retention_period = var.backup_retention_period

  lifecycle {
    ignore_changes = [password]
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-mysql"
    Project     = var.project_name
    Environment = var.environment
  }
}