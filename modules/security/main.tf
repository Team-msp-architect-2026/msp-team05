# ALB Security Group
resource "aws_security_group" "alb" {
  name        = "${var.project_name}-${var.environment}-alb-sg"
  description = "ALB Security Group"
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.project_name}-${var.environment}-alb-sg"
    Project     = var.project_name
    Environment = var.environment
  }
}

# EC2 Security Group
resource "aws_security_group" "ec2" {
  name        = "${var.project_name}-${var.environment}-ec2-sg"
  description = "EC2 Security Group"
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.project_name}-${var.environment}-ec2-sg"
    Project     = var.project_name
    Environment = var.environment
  }
}

# RDS Security Group
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${var.environment}-rds-sg"
  description = "RDS Security Group"
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.project_name}-${var.environment}-rds-sg"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Redis Security Group
resource "aws_security_group" "redis" {
  name        = "${var.project_name}-${var.environment}-redis-sg"
  description = "Redis Security Group"
  vpc_id      = var.vpc_id

  tags = {
    Name        = "${var.project_name}-${var.environment}-redis-sg"
    Project     = var.project_name
    Environment = var.environment
  }
}

/*
# ALB 인바운드 - HTTP
resource "aws_security_group_rule" "alb_ingress_http" {
  type              = "ingress"
  security_group_id = aws_security_group.alb.id
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  prefix_list_ids   = ["pl-93a247fa"]
  description       = "CloudFront Only"
}

# ALB 인바운드 - HTTPS
resource "aws_security_group_rule" "alb_ingress_https" {
  type              = "ingress"
  security_group_id = aws_security_group.alb.id
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  prefix_list_ids   = ["pl-93a247fa"]
  description       = "CloudFront Only"
}
*/

# ALB 인바운드 - HTTP
resource "aws_security_group_rule" "alb_ingress_http" {
  type              = "ingress"
  security_group_id = aws_security_group.alb.id
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "HTTP inbound"
}

# ALB 인바운드 - HTTPS
resource "aws_security_group_rule" "alb_ingress_https" {
  type              = "ingress"
  security_group_id = aws_security_group.alb.id
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "HTTPS inbound"
}

# ALB 아웃바운드 - EC2 8080만
resource "aws_security_group_rule" "alb_egress_ec2" {
  type                     = "egress"
  security_group_id        = aws_security_group.alb.id
  from_port                = 8080
  to_port                  = 8080
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ec2.id
  description              = "ALB to EC2 only"
}

# EC2 인바운드 - ALB에서만 8080
resource "aws_security_group_rule" "ec2_ingress_alb" {
  type                     = "ingress"
  security_group_id        = aws_security_group.ec2.id
  from_port                = 8080
  to_port                  = 8080
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.alb.id
  description              = "Allow from ALB only"
}

# EC2 아웃바운드 - SSM 443
resource "aws_security_group_rule" "ec2_egress_ssm" {
  type              = "egress"
  security_group_id = aws_security_group.ec2.id
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "SSM communication"
}

# EC2 아웃바운드 - RDS 3306
resource "aws_security_group_rule" "ec2_egress_rds" {
  type                     = "egress"
  security_group_id        = aws_security_group.ec2.id
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.rds.id
  description              = "Access to RDS"
}

# EC2 아웃바운드 - Redis 6379
resource "aws_security_group_rule" "ec2_egress_redis" {
  type                     = "egress"
  security_group_id        = aws_security_group.ec2.id
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.redis.id
  description              = "Access to Redis"
}

# RDS 인바운드 - EC2에서만 3306
resource "aws_security_group_rule" "rds_ingress_ec2" {
  type                     = "ingress"
  security_group_id        = aws_security_group.rds.id
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ec2.id
  description              = "Allow from EC2 only"
}

# RDS 아웃바운드 - 전체 허용
resource "aws_security_group_rule" "rds_egress" {
  type              = "egress"
  security_group_id = aws_security_group.rds.id
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "DB response"
}

# Redis 인바운드 - EC2에서만 6379
resource "aws_security_group_rule" "redis_ingress_ec2" {
  type                     = "ingress"
  security_group_id        = aws_security_group.redis.id
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  source_security_group_id = aws_security_group.ec2.id
  description              = "Allow from EC2 only"
}

# Redis 아웃바운드 - 전체 허용
resource "aws_security_group_rule" "redis_egress" {
  type              = "egress"
  security_group_id = aws_security_group.redis.id
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "Cache response"
}
