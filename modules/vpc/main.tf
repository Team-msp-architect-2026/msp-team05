# VPC 생성
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "${var.project_name}-${var.environment}-vpc"
    Project     = var.project_name
    Environment = var.environment
  }
}

# 퍼블릭 서브넷 생성
resource "aws_subnet" "public" {
  count                   = length(var.public_subnet_cidrs)
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name        = "${var.project_name}-${var.environment}-public-subnet-${count.index + 1}"
    Project     = var.project_name
    Environment = var.environment
  }
}

# 프라이빗 서브넷 WAS 생성
resource "aws_subnet" "private_was" {
  count             = length(var.private_subnet_was_cidrs)
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_was_cidrs[count.index]
  availability_zone = var.availability_zones[count.index]

  tags = {
    Name        = "${var.project_name}-${var.environment}-private-was-subnet-${count.index + 1}"
    Project     = var.project_name
    Environment = var.environment
  }
}

# 프라이빗 서브넷 DB 생성
resource "aws_subnet" "private_db" {
  count             = length(var.private_subnet_db_cidrs)
  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_db_cidrs[count.index]
  availability_zone = var.availability_zones[count.index]

  tags = {
    Name        = "${var.project_name}-${var.environment}-private-db-subnet-${count.index + 1}"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Internet Gateway 생성
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name        = "${var.project_name}-${var.environment}-igw"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Elastic IP - AZ-A, AZ-C 각각 1개씩
resource "aws_eip" "nat" {
  count  = 2
  domain = "vpc"

  tags = {
    Name        = "${var.project_name}-${var.environment}-nat-eip-${count.index + 1}"
    Project     = var.project_name
    Environment = var.environment
  }
}

# NAT Gateway - AZ-A, AZ-C 각각 1개씩
resource "aws_nat_gateway" "main" {
  count         = 2
  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id

  tags = {
    Name        = "${var.project_name}-${var.environment}-nat-${count.index + 1}"
    Project     = var.project_name
    Environment = var.environment
  }

  depends_on = [aws_internet_gateway.main]
}

# 퍼블릭 라우트 테이블
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-public-rt"
    Project     = var.project_name
    Environment = var.environment
  }
}

# 퍼블릭 서브넷 라우트 테이블 연결
resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# 프라이빗 라우트 테이블 - AZ-A, AZ-C 각각
resource "aws_route_table" "private" {
  count  = 2
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main[count.index].id
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-private-rt-${count.index + 1}"
    Project     = var.project_name
    Environment = var.environment
  }
}

# 프라이빗 WAS 서브넷 라우트 테이블 연결 (각 AZ 전용 RT 사용)
resource "aws_route_table_association" "private_was" {
  count          = length(aws_subnet.private_was)
  subnet_id      = aws_subnet.private_was[count.index].id
  route_table_id = aws_route_table.private[count.index].id
}

# 프라이빗 DB 서브넷 라우트 테이블 연결 (각 AZ 전용 RT 사용)
resource "aws_route_table_association" "private_db" {
  count          = length(aws_subnet.private_db)
  subnet_id      = aws_subnet.private_db[count.index].id
  route_table_id = aws_route_table.private[count.index].id
}