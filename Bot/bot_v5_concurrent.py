"""
Bot v5 - 분산 요청 시뮬레이션 (Concurrent + IP Rotation)
asyncio 기반 동시 요청 + X-Forwarded-For IP 위조
각 봇이 독립적으로 전체 흐름 실행 (수치/지표 측정용)
"""

import asyncio
import aiohttp
import time
import random
import sys
from headers import get_random_profile

BASE_URL    = "https://kkybot.click"
TARGET_TEAM = None

def random_ip():
    return f"{random.randint(1,254)}.{random.randint(0,254)}.{random.randint(0,254)}.{random.randint(1,254)}"

IP_POOL = [random_ip() for _ in range(50)]


def make_headers(token=None):
    h = get_random_profile()
    h["X-Forwarded-For"]  = random.choice(IP_POOL)
    h["X-Real-IP"]        = random.choice(IP_POOL)
    h["X-Originating-IP"] = random.choice(IP_POOL)
    if token:
        h["Authorization"] = f"Bearer {token}"
    h["Content-Type"] = "application/json"
    return h


async def bot_worker(session, worker_id, results):
    ts     = int(time.time() * 1000) + worker_id
    email  = f"botv5_{ts}@test.com"
    passwd = "botv5pass1"
    ip     = random.choice(IP_POOL)

    try:
        # 1. 게임 조회
        async with session.get(f"{BASE_URL}/api/games", headers=make_headers()) as res:
            if res.status == 202:
                results.append({"worker": worker_id, "status": "CAPTCHA", "step": "GET_GAMES", "ip": ip})
                print(f"  [W{worker_id:03d}] GET_GAMES CAPTCHA (202) - WAF 차단")
                return
            if res.status == 403:
                results.append({"worker": worker_id, "status": "BLOCK", "step": "GET_GAMES", "ip": ip})
                print(f"  [W{worker_id:03d}] GET_GAMES BLOCK (403) - WAF 차단")
                return
            data = await res.json(content_type=None)
            games = data.get("data", {}).get("content", [])
            on_sale = [g for g in games if g.get("status") == "ON_SALE"]
            if not on_sale:
                return
            game_id = on_sale[0]["gameId"]

        # 2. 회원가입
        async with session.post(f"{BASE_URL}/api/auth/signup",
                json={"email": email, "passwd": passwd,
                      "username": f"bot{worker_id}", "phonenum": "010-0000-0000"},
                headers=make_headers()) as res:
            if res.status == 202:
                results.append({"worker": worker_id, "status": "CAPTCHA", "step": "SIGNUP", "ip": ip})
                print(f"  [W{worker_id:03d}] SIGNUP CAPTCHA (202) - WAF 차단")
                return
            if res.status == 403:
                results.append({"worker": worker_id, "status": "BLOCK", "step": "SIGNUP", "ip": ip})
                print(f"  [W{worker_id:03d}] SIGNUP BLOCK (403) - WAF 차단")
                return

        # 3. 로그인
        async with session.post(f"{BASE_URL}/api/auth/login",
                json={"email": email, "passwd": passwd},
                headers=make_headers()) as res:
            if res.status == 202:
                results.append({"worker": worker_id, "status": "CAPTCHA", "step": "LOGIN", "ip": ip})
                print(f"  [W{worker_id:03d}] LOGIN CAPTCHA (202) - WAF 차단")
                return
            if res.status == 403:
                results.append({"worker": worker_id, "status": "BLOCK", "step": "LOGIN", "ip": ip})
                print(f"  [W{worker_id:03d}] LOGIN BLOCK (403) - WAF 차단")
                return
            data = await res.json(content_type=None)
            if not data.get("success"):
                results.append({"worker": worker_id, "status": "LOGIN_FAIL", "step": "LOGIN", "ip": ip})
                return
            token = data["data"]["accessToken"]

        # 4. 좌석 조회
        async with session.get(f"{BASE_URL}/api/games/{game_id}/seats",
                headers=make_headers(token)) as res:
            if res.status in (202, 403):
                status = "CAPTCHA" if res.status == 202 else "BLOCK"
                results.append({"worker": worker_id, "status": status, "step": "GET_SEATS", "ip": ip})
                print(f"  [W{worker_id:03d}] GET_SEATS {status} ({res.status}) - WAF 차단")
                return
            data = await res.json(content_type=None)
            all_seats = []
            for zone in data.get("data", {}).get("zones", []):
                all_seats.extend([s["seatId"] for s in zone.get("seats", [])
                                   if s.get("status") == "AVAILABLE"])
            if not all_seats:
                return
            seat_id = random.choice(all_seats)

        # 5. 좌석 선점
        h = make_headers(token)
        async with session.post(f"{BASE_URL}/api/games/{game_id}/seats/lock",
                json={"seatIds": [seat_id]},
                headers=h) as res:
            if res.status == 202:
                results.append({"worker": worker_id, "status": "CAPTCHA", "step": "LOCK", "ip": ip})
                print(f"  [W{worker_id:03d}] LOCK CAPTCHA (202) - WAF 차단")
                return
            if res.status == 403:
                results.append({"worker": worker_id, "status": "BLOCK", "step": "LOCK", "ip": ip})
                print(f"  [W{worker_id:03d}] LOCK BLOCK (403) - WAF 차단")
                return
            if res.status == 200:
                body = await res.json(content_type=None)
                lock_token = body.get("data", {}).get("lockToken")
                amount = body.get("data", {}).get("totalAmount", 0)
                results.append({"worker": worker_id, "status": 200, "step": "LOCK", "ip": ip})
                print(f"  [W{worker_id:03d}] LOCK 성공 | IP={ip}")

                async with session.post(f"{BASE_URL}/api/payments/prepare",
                        json={"lockToken": lock_token, "paymentMethod": "CARD"},
                        headers=make_headers(token)) as res2:
                    print(f"  [W{worker_id:03d}] PAYMENT {res2.status} | 금액={amount:,}원")
            else:
                results.append({"worker": worker_id, "status": res.status, "step": "LOCK", "ip": ip})

    except Exception as e:
        results.append({"worker": worker_id, "status": "ERROR", "step": "UNKNOWN", "ip": ip, "error": str(e)})
        print(f"  [W{worker_id:03d}] ERROR: {e}")


async def run(concurrency=10):
    print("=" * 55)
    print(f"Bot v5 - 동시 요청 {concurrency}개 + IP 로테이션")
    print("=" * 55)

    connector = aiohttp.TCPConnector(limit=concurrency)
    async with aiohttp.ClientSession(connector=connector) as session:

        results = []
        start   = time.time()

        tasks = [bot_worker(session, i, results)
                 for i in range(concurrency)]
        await asyncio.gather(*tasks)

        elapsed = time.time() - start

    print("\n" + "=" * 55)
    success = sum(1 for r in results if r["status"] == 200)
    blocked = sum(1 for r in results if r["status"] == 429)
    captcha = sum(1 for r in results if r["status"] == "CAPTCHA")
    block   = sum(1 for r in results if r["status"] == "BLOCK")
    failed  = sum(1 for r in results if r["status"] not in [200, 429, "CAPTCHA", "BLOCK"])

    print(f"[결과] 동시 요청: {concurrency}개 | 소요: {elapsed:.2f}s")
    print(f"  성공(200): {success}  |  차단(429): {blocked}  |  CAPTCHA(202): {captcha}  |  BLOCK(403): {block}  |  기타 실패: {failed}")
    print(f"  WAF 차단율: {(captcha + blocked + block) / concurrency * 100:.1f}%")
    print(f"  처리량: {concurrency / elapsed:.1f} rps")

    steps = {}
    for r in results:
        if r["status"] in ("CAPTCHA", "BLOCK"):
            step = r.get("step", "UNKNOWN")
            steps[step] = steps.get(step, 0) + 1
    if steps:
        print(f"\n[WAF 차단 단계별 현황]")
        for step, count in steps.items():
            print(f"  {step}: {count}개 차단")

    ip_stat = {}
    for r in results:
        ip = r.get("ip", "")
        if not ip:
            continue
        if ip not in ip_stat:
            ip_stat[ip] = {"success": 0, "blocked": 0}
        if r["status"] == 200:
            ip_stat[ip]["success"] += 1
        elif r["status"] in ("CAPTCHA", "BLOCK", 429):
            ip_stat[ip]["blocked"] += 1

    unique_ips   = len(ip_stat)
    blocked_ips  = [ip for ip, v in ip_stat.items() if v["blocked"] > 0]

    print(f"\n[IP 로테이션]")
    print(f"  사용된 IP  : {unique_ips}개")
    print(f"  차단된 IP  : {len(blocked_ips)}개")
    print(f"  우회된 IP  : {unique_ips - len(blocked_ips)}개")

    if captcha > 0 or block > 0 or blocked > 0:
        print(f"\n[WAF] 차단됨! CAPTCHA: {captcha}개 | BLOCK: {block}개")
    else:
        print(f"\n[WAF] 차단 없음 - {concurrency}rps 통과")


if __name__ == "__main__":
    concurrency = int(sys.argv[1]) if len(sys.argv) > 1 else 10
    asyncio.run(run(concurrency=concurrency))