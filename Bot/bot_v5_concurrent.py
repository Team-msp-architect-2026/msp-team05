"""
Bot v5 - 분산 요청 시뮬레이션 (Concurrent + IP Rotation)
asyncio 기반 동시 요청 + X-Forwarded-For IP 위조
GAME_ID 자동 감지 (하드코딩 없음)
"""

import asyncio
import aiohttp
import time
import random
import sys
from headers import get_random_profile

BASE_URL    = "https://kkybot.click"
#BASE        = "http://localhost:8080"
TARGET_TEAM = None   # None = 첫 번째 ON_SALE 게임 / "LG" = LG 홈경기만 타겟

# ── IP 풀 (랜덤 생성) ─────────────────────────────
def random_ip():
    return f"{random.randint(1,254)}.{random.randint(0,254)}.{random.randint(0,254)}.{random.randint(1,254)}"

IP_POOL = [random_ip() for _ in range(50)]


# ── 헤더 생성 (UA + IP 위조) ─────────────────────
def make_headers(token=None):
    h = get_random_profile()
    h["X-Forwarded-For"]  = random.choice(IP_POOL)
    h["X-Real-IP"]        = random.choice(IP_POOL)
    h["X-Originating-IP"] = random.choice(IP_POOL)
    if token:
        h["Authorization"] = f"Bearer {token}"
    h["Content-Type"] = "application/json"
    return h


# ── GAME_ID 자동 감지 ─────────────────────────────
async def fetch_game_id(session) -> str:
    async with session.get(f"{BASE}/api/games", headers=make_headers()) as res:
        data = await res.json()
        games = data.get("data", {}).get("content", [])
        for game in games:
            if game.get("status") != "ON_SALE":
                continue
            if TARGET_TEAM and TARGET_TEAM not in game.get("homeTeam", {}).get("name", ""):
                continue
            game_id = game["gameId"]
            home    = game.get("homeTeam", {}).get("name", "?")
            away    = game.get("awayTeam", {}).get("name", "?")
            start   = game.get("startTime", "?")
            print(f"[GAME] 감지: {home} vs {away} | {start}")
            print(f"[GAME] gameId = {game_id}")
            return game_id
    raise RuntimeError("[ERROR] ON_SALE 게임 없음 - 시드 데이터 확인")


# ── 좌석 목록 조회 ────────────────────────────────
async def fetch_seats(session, game_id: str):
    async with session.get(f"{BASE}/api/games/{game_id}/seats",
            headers=make_headers()) as res:
        data = await res.json()
        all_seats = []
        for zone in data.get("data", {}).get("zones", []):
            all_seats.extend([s["seatId"] for s in zone.get("seats", [])
                               if s.get("status") == "AVAILABLE"])
        print(f"[SEATS] 예약 가능 좌석: {len(all_seats)}개")
        return all_seats


# ── 단일 봇 워커 ──────────────────────────────────
async def bot_worker(session, worker_id, game_id, seats, results):
    ts     = int(time.time() * 1000) + worker_id
    email  = f"botv5_{ts}@test.com"
    passwd = "botv5pass1"

    try:
        # 1. 회원가입
        async with session.post(f"{BASE}/api/auth/signup",
                json={"email": email, "passwd": passwd,
                      "username": f"bot{worker_id}", "phonenum": "010-0000-0000"},
                headers=make_headers()) as res:
            pass

        # 2. 로그인
        async with session.post(f"{BASE}/api/auth/login",
                json={"email": email, "passwd": passwd},
                headers=make_headers()) as res:
            data = await res.json()
            if not data.get("success"):
                results.append({"worker": worker_id, "status": "LOGIN_FAIL", "ip": ""})
                return
            token = data["data"]["accessToken"]

        # 3. 좌석 선점 (랜덤 좌석 1개)
        if not seats:
            return
        seat_id = random.choice(seats)
        h = make_headers(token)

        lock_token = None
        amount     = 0
        async with session.post(f"{BASE}/api/games/{game_id}/seats/lock",
                json={"seatIds": [seat_id]},
                headers=h) as res:
            status_code = res.status
            if status_code == 200:
                body       = await res.json()
                lock_token = body.get("data", {}).get("lockToken")
                amount     = body.get("data", {}).get("totalAmount", 0)
            results.append({
                "worker": worker_id,
                "status": status_code,
                "ip":     h["X-Forwarded-For"],
                "seat":   seat_id[:8],
            })
            print(f"  [W{worker_id:03d}] LOCK {status_code} | IP={h['X-Forwarded-For']} | UA={h['User-Agent'][11:40]}...")

        # 4. 결제 준비 (LOCK 성공 시)
        if lock_token:
            async with session.post(f"{BASE}/api/payments/prepare",
                    json={"lockToken": lock_token, "paymentMethod": "CARD"},
                    headers=make_headers(token)) as res:
                pay_code = res.status
                print(f"  [W{worker_id:03d}] PAYMENT {pay_code} | 금액={amount:,}원 | 상태=READY")

    except Exception as e:
        results.append({"worker": worker_id, "status": "ERROR", "ip": "", "error": str(e)})
        print(f"  [W{worker_id:03d}] ERROR: {e}")


# ── 메인 실행 ─────────────────────────────────────
async def run(concurrency=10):
    print("=" * 55)
    print(f"Bot v5 - 동시 요청 {concurrency}개 + IP 로테이션")
    print("=" * 55)

    connector = aiohttp.TCPConnector(limit=concurrency)
    async with aiohttp.ClientSession(connector=connector) as session:

        # GAME_ID 자동 감지
        game_id = await fetch_game_id(session)

        # 좌석 조회
        seats = await fetch_seats(session, game_id)
        if not seats:
            print("[ERROR] 좌석 정보 없음")
            return

        results = []
        start   = time.time()

        # 동시 요청 실행
        tasks = [bot_worker(session, i, game_id, seats, results)
                 for i in range(concurrency)]
        await asyncio.gather(*tasks)

        elapsed = time.time() - start

    # ── 결과 집계 ──────────────────────────────────
    print("\n" + "=" * 55)
    success = sum(1 for r in results if r["status"] == 200)
    blocked = sum(1 for r in results if r["status"] == 429)
    failed  = sum(1 for r in results if r["status"] not in [200, 429])

    print(f"[결과] 동시 요청: {concurrency}개 | 소요: {elapsed:.2f}s")
    print(f"  성공(200): {success}  |  차단(429): {blocked}  |  기타 실패: {failed}")
    print(f"  처리량: {concurrency / elapsed:.1f} rps")

    # ── IP 로테이션 우회 확인 ──────────────────────
    ip_stat = {}
    for r in results:
        ip = r.get("ip", "")
        if not ip:
            continue
        if ip not in ip_stat:
            ip_stat[ip] = {"success": 0, "blocked": 0}
        if r["status"] == 200:
            ip_stat[ip]["success"] += 1
        elif r["status"] == 429:
            ip_stat[ip]["blocked"] += 1

    unique_ips   = len(ip_stat)
    blocked_ips  = [ip for ip, v in ip_stat.items() if v["blocked"] > 0]
    bypassed_ips = unique_ips - len(blocked_ips)

    print(f"\n[IP 로테이션]")
    print(f"  사용된 IP  : {unique_ips}개")
    print(f"  차단된 IP  : {len(blocked_ips)}개")
    print(f"  우회된 IP  : {bypassed_ips}개")
    if unique_ips > 0:
        print(f"  우회율     : {bypassed_ips / unique_ips * 100:.1f}%")

    if blocked > 0:
        print(f"\n[WAF] 차단 감지! 임계값 = {concurrency}rps 이하")
    else:
        print(f"\n[WAF] 차단 없음 - {concurrency}rps 통과")


if __name__ == "__main__":
    # 사용법: python bot_v5_concurrent.py [동시요청수]
    # 예: python bot_v5_concurrent.py 10
    #     python bot_v5_concurrent.py 50
    #     python bot_v5_concurrent.py 100
    concurrency = int(sys.argv[1]) if len(sys.argv) > 1 else 10
    asyncio.run(run(concurrency=concurrency))