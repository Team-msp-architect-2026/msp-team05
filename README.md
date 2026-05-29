<div align="center">

# ⚾ KKY 야구 티켓팅
### AWS WAF Bot Control 매크로 봇 방어 실측 프로젝트
**MSP Architect Training 2026 · Team 05**

---

![Frontend](https://img.shields.io/badge/Frontend-React%20%7C%20TypeScript-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%203.2.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20Redis-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Infra](https://img.shields.io/badge/Infra-AWS%20%7C%20Terraform-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Deploy](https://img.shields.io/badge/Deploy-Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![WAF](https://img.shields.io/badge/WAF-Bot%20Control%20%7C%20JA4%20Fingerprinting-DD344C?style=for-the-badge&logo=amazonaws&logoColor=white)
![Monitoring](https://img.shields.io/badge/Monitoring-CloudWatch%20%7C%20Athena-F46800?style=for-the-badge&logo=amazonaws&logoColor=white)

</div>

---

## 📌 배경

### 🏢 기업 관점

티켓팅 서비스를 운영하다 보면 오픈 순간마다 같은 상황이 반복된다.  
매크로 봇이 대량 요청으로 좌석을 선점하고, 정상 사용자는 티켓을 구하지 못한다.  
독점된 티켓은 암표 시장에서 몇 배의 가격으로 거래되고,  
**결국 서비스에 대한 신뢰도 하락으로 이어진다.**

### 👤 사용자 관점

티켓 오픈 시간에 맞춰 대기하고, 정각에 버튼을 눌렀는데 이미 매진이다.  
분명히 오픈 직후였는데 어떻게 된 건지 알 수가 없다.  
매크로 봇이 이미 모든 좌석을 점유한 뒤였다.  
얼마 지나지 않아 같은 티켓이 몇 배의 가격으로 암표 시장에 올라온다.

---

## 🎯 프로젝트 목표

매크로 봇 문제를 해결하기 위한 솔루션은 존재하지만,  
**AWS WAF Bot Control만으로 어느 수준까지 방어 가능한지 검증된 국내 실측 데이터는 없다.**

카오스 엔지니어링을 통해 WAF Bot Control이 어느 수준의 매크로 봇까지 막는지 한계를 확인하고,  
**탐지율(TPR) / 오탐률(FPR) 수치로 국내 기업의 도입 판단 근거를 만든다.**

---

## 💡 기대 효과

- **국내 최초 실측 데이터 제공** — AWS WAF Bot Control의 실제 방어 수치를 공개하여 국내 기업의 도입 결정 근거 마련
- **비용 대비 효과 검증** — 트래픽에 따라 달라지는 실제 비용과 방어 수준을 수치로 확인
- **오탐률 수치화** — 정상 사용자가 차단될 위험도를 수치로 제시하여 도입 리스크 해소

---

## 🧪 검증 시나리오

| # | 방식 | 내용 |
|---|------|------|
| 1 | Before / After | 단순 HTTP 봇 → Selenium → Playwright + IP 로테이션 단계별 탐지율 비교 |
| 2 | Chaos Engineering | 봇 + 정상 사용자 동시 대량 트래픽 → 서비스 안정성 및 WAF 차단 검증 |
| 3 | Blue / Green | 자동 대응 파이프라인 유무 환경 비교 → 대응 속도 및 피해 범위 측정 |

---

## 📊 성공 기준 (KPI)

| 항목 | 기준 |
|------|------|
| 1단계 봇 (Python requests) 탐지율 | ≥ 95% |
| 2단계 봇 (Selenium) 탐지율 | ≥ 80% |
| 3단계 봇 (Playwright + 프록시) 탐지율 | ≥ 50% |
| 정상 사용자 오탐률 | ≤ 5% |
| 자동 대응 속도 | ≤ 60초 |
| 전체 테스트 비용 | $100 이하 |

---

## 🏗️ 시스템 아키텍처

<img width="950" height="850" alt="image" src="https://github.com/user-attachments/assets/cab75c62-71e6-4213-a746-6dc888bd0cd9" />



<!-- WAF 자동 대응 파이프라인 이미지 삽입 -->

> 📖 상세 아키텍처는 [Wiki — 시스템 아키텍처](../../wiki/시스템-아키텍처) 참조

---

## 🛠️ 기술 스택

| 계층 | 기술 |
|------|------|
| **Frontend** | React TypeScript · Vite · Tailwind CSS |
| **Backend** | Spring Boot 3.2.4 · JPA · Spring Security · JWT |
| **Database** | RDS MySQL 8.0 · ElastiCache Redis 7.x |
| **WAF / Security** | AWS WAF Bot Control Targeted · JA4 핑거프린팅 · CloudFront · Geo IP 차단 |
| **Infra** | EC2 + Auto Scaling · ALB · S3 · Secrets Manager · Cognito |
| **IaC** | Terraform |
| **분석** | Athena · CloudWatch |
| **자동 대응** | CloudWatch → SNS → Lambda → WAF |
| **봇 시뮬레이션** | Python requests · Selenium · Playwright |

---

## 👥 팀원

| 역할 | 이름 | 주요 담당 | GitHub |
|------|------|-----------|--------|
| 팀장 | 김재훈 | | |
| 팀원 | 김수경 | | |
| 팀원 | 양준표 | | |

---

## 📚 문서

자세한 내용은 **[Wiki](../../wiki)** 를 참고해주세요.  
프로젝트 진행 현황 → **[Project Board](https://github.com/Team-msp-architect-2026/msp-team05/projects)**
