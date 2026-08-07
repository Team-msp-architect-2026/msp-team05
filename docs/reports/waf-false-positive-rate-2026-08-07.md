# WAF 오탐률(False Positive Rate) 측정 보고서

- 측정일: 2026-08-07 (KST 2026-08-08 새벽)
- 대상: `kkybot.net` CloudFront WebACL (`kky-prod-waf`)
- 적용 규칙: GeoBlockRule(우선순위 0) → BotControlRule(`AWSManagedRulesBotControlRuleSet`, TARGETED, 우선순위 1) → RateBasedRule(우선순위 2) → HealthCheckExceptionRule(우선순위 3)
- 목적: 정상 사용자(자동화 흔적 없는 실제 브라우저)가 WAF에 얼마나 오탐(false positive)되는지 측정

## 1. 테스트 방법

- Playwright Chromium, **`headless=False`** (실제 GUI 브라우저 창), stealth 플러그인 미적용(순정 브라우저)
- 세션마다 새 `BrowserContext` 생성 (쿠키/localStorage 초기화 → 매번 "신규 방문자"로 취급)
- 플로우: `/login` 실제 페이지 로드(WAF `challenge.js` 실행 포함) → 이메일/비밀번호 폼 입력 → 제출 → 로그인 API 응답 코드 기록 → (로그인 성공 시) 발급된 토큰으로 경기 목록·좌석 목록 API 호출
- 반복 횟수: 50회
- 테스트 소스 IP: `1.241.92.17` (세션 내내 봇 테스트(v1~v7, 수백~수천 건)를 반복 실행해온 것과 동일한 IP)

## 2. 1차 결과 — 잘못된 판정 기준으로 계산한 값

봇 스크립트(v1~v7)들은 전부 WAF 차단 판정 기준을 `HTTP 202(CAPTCHA)` 또는 `HTTP 403(Block)`으로 하드코딩하고 있었다. 이 기준을 그대로 적용하면:

| 항목 | 값 |
|---|---|
| 총 요청 수 | 122 |
| 차단(202/403) 건수 | 0 |
| **오탐률 (1차, 잘못된 계산)** | **0.00%** |

하지만 실제로는 50개 세션 중 42개(84%)에서 로그인 자체가 실패(`HTTP 405`)하고 있었다. 202/403만 보는 필터로는 이 405가 전부 누락되어 "오탐 없음"으로 잘못 보고될 뻔했다.

## 3. 핵심 발견 — `HTTP 405`가 사실은 WAF CAPTCHA 차단이었다

실패 응답의 헤더를 직접 확인한 결과:

```
HTTP/2 405
x-amzn-waf-action: captcha
x-cache: Error from cloudfront
server: CloudFront
content-type: text/html; charset=UTF-8
```

응답 바디도 실제 "Human Verification" CAPTCHA 인터스티셜 페이지였다. 즉 **이 환경의 AWS WAF는 CAPTCHA 챌린지를 표준으로 알려진 202가 아니라 405로 반환하는 경우가 있다.** `x-amzn-waf-action` 응답 헤더가 실제 WAF 판정을 가장 신뢰성 있게 알려주는 지표이며, 상태 코드(202/403/405)만으로 차단 여부를 판단하는 기존 방식은 근본적으로 불완전하다.

**시사점**: v1~v7 봇 스크립트의 `check_waf_block()` 로직(202/403/405/429만 체크)은 이 케이스는 이미 커버하고 있었지만(405도 블록으로 분류), 이번 정상 사용자 시뮬레이션 스크립트는 202/403만 체크하도록 작성되어 최초 계산에서 이 신호를 놓쳤다. **향후 모든 WAF 관련 측정은 상태 코드가 아니라 `x-amzn-waf-action` 헤더 유무로 판정해야 한다.**

## 4. 정정된 결과

| 항목 | 건수 | 비율 |
|---|---|---|
| 총 세션 | 50 | 100% |
| 로그인 성공 (200) | 2 | 4% |
| **WAF CAPTCHA 차단 (405, `x-amzn-waf-action: captcha`)** | **42** | **84%** |
| 브라우저 크래시 (판정 불가, WAF와 무관한 환경 이슈) | 6 | 12% |

판정 가능한 세션(44건)만 기준으로 하면:

**오탐률 = 42 / 44 ≈ 95.5%**

자동화 흔적을 전혀 남기지 않은 순정 Chrome 브라우저로, WAF의 자체 JS SDK 챌린지(`challenge.js`, `mp_verify`)까지 매번 정상 실행됐음에도 불구하고, 실제 로그인 시도의 약 95%가 CAPTCHA로 차단되었다.

## 5. IP 평판 오염 가설

이 수치를 "일반적인 정상 사용자 오탐률"로 일반화하기는 위험하다. 테스트에 사용한 IP(`1.241.92.17`)는 같은 세션 안에서 v1~v7 봇 스크립트로 **수백~수천 건의 자동화 트래픽**(동시 요청 100건짜리 부하 테스트 다회 포함)을 이미 쏟아부은 이력이 있다.

`BotControlRule`의 override 규칙 `TGT_TokenReuseIp`, `TGT_VolumetricSession`는 이름 그대로 **IP/세션 단위의 과거 행동 패턴**을 근거로 판정하는 룰이다. 즉 이번에 측정된 84~95%라는 극단적으로 높은 차단율은:

- (가설 A) WAF Bot Control이 정상 사용자도 광범위하게 오탐하는 구조적 문제이거나
- (가설 B) **오늘 이 IP가 봇 테스트로 이미 "요주의 IP/세션"으로 학습되어**, 지금 이 IP로는 실제 사람이 접속해도 계속 CAPTCHA를 받는 상태이거나

둘 중 하나이며, 현재 테스트만으로는 구분할 수 없다.

**검증 필요**: 오늘 봇 트래픽 이력이 전혀 없는 클린 IP(VPN/프록시 등)로 동일 테스트를 최소 횟수(1~10회)로 재실행해서 비교해야 실제 오탐률을 신뢰성 있게 확정할 수 있다. 클린 IP 테스트는 반복할수록 그 IP도 함께 오염되어 측정 의미가 사라지므로, 실행 시 최소 횟수 원칙을 지켜야 한다. (이 보고서 작성 시점 기준 클린 IP 테스트는 아직 미실행 — VPN/프록시 준비 대기 중)

## 6. 관련하여 이 세션에서 함께 발견·수정한 WAF 파이프라인 버그

같은 세션에서 CloudWatch → SNS → Lambda → WAF 자동 대응 파이프라인을 검증하며 아래 버그도 함께 발견해 수정했다 (별도 태스크, 참고용):

1. `aws_cloudwatch_metric_alarm.waf_blocked`가 기본 provider(ap-northeast-2)로 생성되어 있었음 — CloudFront WAF 지표는 us-east-1에만 존재해 알람이 영구 `INSUFFICIENT_DATA`였음. `provider = aws.us_east_1` 추가로 해결.
2. 알람 `dimensions`에 실존하지 않는 `Region = "us-east-1"` 차원이 포함되어 있었음 — 제거.
3. us-east-1 알람의 `alarm_actions`가 ap-northeast-2 SNS 토픽을 참조해 `ValidationError`로 생성 자체가 실패 — us-east-1 전용 SNS 토픽(`kky-prod-waf-alarm-use1`)을 신설해 연결.
4. Lambda IAM 정책의 `Resource` 키 중복 선언으로 인해 `wafv2:UpdateWebACL` 호출이 매번 `AccessDeniedException`으로 실패 — WebACL ARN과 계정 소유 `managedruleset/*/*` ARN을 리스트로 함께 허용하도록 수정.

수정 후 `aws cloudwatch set-alarm-state`로 강제 트리거해 검증한 결과, 알람 발생부터 `RateBasedRule` limit 자동 조정(1000→100) 완료까지 **약 2.6초** (목표 60초 대비 23배 빠름) 소요되는 것을 확인했다.

## 7. 권장 사항

1. **오탐률 재측정**: 클린 IP로 1~10회 이내 최소 재현 테스트를 진행해 가설 A/B 중 무엇이 맞는지 확정할 것.
2. **판정 기준 표준화**: 모든 WAF 관련 봇/오탐 테스트 스크립트가 상태 코드가 아니라 `x-amzn-waf-action` 응답 헤더를 1차 판정 기준으로 쓰도록 통일할 것.
3. 가설 B(IP 평판 오염)가 사실로 확인될 경우, `TGT_TokenReuseIp`/`TGT_VolumetricSession`의 행동 이력 누적 기간(cooldown)을 파악하고, 운영 중 정상 사용자가 일시적 트래픽 급증만으로 장시간 차단되지 않도록 규칙 튜닝을 검토할 것.
