# Bot 테스트 환경

## 환경 구성

python -m venv venv
.\venv\Scripts\activate
pip install -r requirements.txt

## 봇 목록

| 봇 | 파일 | 담당 |
|----|------|------|
| Bot v1: HTTP 요청 봇 | bot_v1_requests.py | 양준표 |
| Bot v2: Selenium 봇 | bot_v2_selenium.py | 김수경 |
| Bot v3: Playwright 봇 | bot_v3_playwright.py | 양준표 |
| Bot v4: 헤더 위조 봇 | bot_v4_headers.py | 김재훈 |
| Bot v5: 분산 요청 봇 | bot_v5_concurrent.py | 김재훈 |
| Bot v6: WebSocket 봇 | bot_v6_websocket.py | 김수경 |
| Bot v7: TLS Fingerprint 봇 | bot_v7_tls.py | 김수경 |

## 실행 방법

### Bot v2: Selenium 봇

python bot_v2_selenium.py
→ 이메일 입력
→ 비밀번호 입력
→ Headless 모드 선택 (y/n)

### Bot v6: WebSocket 봇

→ 이메일 입력
→ 비밀번호 입력
→ 경기 번호 선택


### Bot v7: TLS Fingerprint 봇
python bot_v7_tls.py
→ 이메일 입력
→ 비밀번호 입력
→ TLS 클라이언트 선택 (1/2/3)

chrome_120
firefox_120
safari_16_0

## 테스트 환경
- 로컬: http://localhost:8080
- AWS: CloudFront URL (WAF 탐지율 측정)