# Cognito User Pool
resource "aws_cognito_user_pool" "main" {
  name = "${var.project_name}-${var.environment}-user-pool"

  # 이메일로 로그인
  username_attributes = ["email"]
  auto_verified_attributes = ["email"]

  # 비밀번호 정책 (8자 이상)
  password_policy {
    minimum_length                   = 8
    require_lowercase                = true
    require_uppercase                = false
    require_numbers                  = false
    require_symbols                  = false
    temporary_password_validity_days = 7
  }

  # 이메일 설정
  email_configuration {
    email_sending_account = "COGNITO_DEFAULT"
  }

  # 사용자 속성 설정
  schema {
    name                = "email"
    attribute_data_type = "String"
    required            = true
    mutable             = true
  }

  schema {
    name                = "name"
    attribute_data_type = "String"
    required            = true
    mutable             = true
  }

  schema {
    name                = "phone_number"
    attribute_data_type = "String"
    required            = false
    mutable             = true
  }

  # 계정 복구 설정
  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-user-pool"
    Project     = var.project_name
    Environment = var.environment
  }
}

# Cognito App Client (백엔드 연동용)
resource "aws_cognito_user_pool_client" "main" {
  name         = "${var.project_name}-${var.environment}-app-client"
  user_pool_id = aws_cognito_user_pool.main.id

  # 시크릿 없음 (Spring Boot 서버 사이드 인증)
  generate_secret = false

  # JWT 토큰 만료 시간
  access_token_validity  = 1    # 1시간
  id_token_validity      = 1    # 1시간
  refresh_token_validity = 7    # 7일

  token_validity_units {
    access_token  = "hours"
    id_token      = "hours"
    refresh_token = "days"
  }

  # 인증 플로우
  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_USER_SRP_AUTH"
  ]

  # 토큰 취소 활성화
  enable_token_revocation = true

  # 사용자 존재 오류 방지 (보안)
  prevent_user_existence_errors = "ENABLED"
}