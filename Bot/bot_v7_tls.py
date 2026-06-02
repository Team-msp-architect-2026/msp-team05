import tls_client
import json
import time

BASE_URL = "https://kkybot.click"
#BASE_URL = "http://localhost:8080"
EMAIL    = input("이메일 입력: ")
PASSWD   = input("비밀번호 입력: ")

TLS_CLIENT = input(
    "TLS 클라이언트 선택\n"
    "1. chrome_120\n"
    "2. firefox_120\n"
    "3. safari_16_0\n"
    "선택 (1/2/3): "
).strip()

TLS_MAP = {
    "1": "chrome_120",
    "2": "firefox_120",
    "3": "safari_16_0"
}
client_id = TLS_MAP.get(TLS_CLIENT, "chrome_120")
print(f"[TLS] 클라이언트: {client_id}")

response_times = []
token = None
game_id = None

session = tls_client.Session(
    client_identifier=client_id,
    random_tls_extension_order=True
)

def login():
    global token
    start = time.time()
    res = session.post(
        f"{BASE_URL}/api/auth/login",
        json={"email": EMAIL, "passwd": PASSWD}
    )
    elapsed = time.time() - start
    response_times.append(elapsed)

    if res.status_code == 200:
        token = res.json()["data"]["accessToken"]
        print(f"[로그인 성공] 소요={elapsed:.3f}초")
        print(f"[토큰] {token[:30]}...")
    else:
        print(f"[로그인 실패] {res.status_code}: {res.text}")
        exit(1)

def get_game():
    global game_id
    res = session.get(
        f"{BASE_URL}/api/games",
        headers={"Authorization": f"Bearer {token}"}
    )
    if res.status_code == 200:
        games = res.json()["data"]["content"]
        on_sale = [
            g for g in games
            if g.get("status") == "ON_SALE"
        ]
        if not on_sale:
            print("[경기 없음] ON_SALE 경기 없음")
            exit(1)
        print("\n[경기 목록]")
        for i, g in enumerate(games):
            home    = g["homeTeam"]["name"]
            away    = g["awayTeam"]["name"]
            stadium = g["stadium"]["name"]
            start_t = g.get("startTime", "")
            print(f"  {i+1}. gameId={g['gameId']} "
                  f"| {home} vs {away} "
                  f"| {stadium} | {start_t}")
        choice  = int(input("\n테스트할 경기 번호 입력: ")) - 1
        game_id = games[choice]["gameId"]
        print(f"[선택] gameId={game_id}")
    else:
        print(f"[경기 조회 실패] {res.status_code}")
        exit(1)

def enter_queue():
    start = time.time()
    res = session.post(
        f"{BASE_URL}/api/queue/enter",
        json={"gameId": game_id},
        headers={"Authorization": f"Bearer {token}"}
    )
    elapsed = time.time() - start
    response_times.append(elapsed)

    if res.status_code == 200:
        queue_token = res.json()["data"]["queueToken"]
        position    = res.json()["data"]["position"]
        print(f"[대기열 진입] 순번={position} 소요={elapsed:.3f}초")
        return queue_token
    else:
        print(f"[대기열 실패] {res.status_code}: {res.text}")
        return None

def wait_for_allowed(queue_token):
    print("[대기열] ALLOWED 상태 대기 중...")
    for i in range(60):
        res = session.get(
            f"{BASE_URL}/api/queue/status/{queue_token}",
            headers={"Authorization": f"Bearer {token}"}
        )
        if res.status_code == 200:
            data   = res.json()["data"]
            status = data["status"]
            print(f"[대기열] 상태={status} 순번={data.get('position', 0)}")
            if status == "ALLOWED":
                print("[대기열] ALLOWED ✅")
                return True
            if status == "EXPIRED":
                print("[대기열] 만료됨")
                return False
        time.sleep(3)
    print("[대기열] 타임아웃")
    return False

def get_seat():
    res = session.get(
        f"{BASE_URL}/api/games/{game_id}/seats",
        headers={"Authorization": f"Bearer {token}"},
        timeout_seconds=300
    )
    if res.status_code != 200:
        print("[구역 조회 실패]")
        return None

    zones = res.json()["data"]["zones"]
    target_zone_id = None

    for zone in zones:
        available = [
            s for s in zone.get("seats", [])
            if s["status"] == "AVAILABLE"
        ]
        if available:
            # 구역에 seats 데이터 있으면 바로 사용
            seat_id = available[0]["seatId"]
            print(f"[좌석] 선택: {zone['zoneName']} "
                  f"seatId={seat_id}")
            return seat_id
        if zone.get("availableCount", 0) > 0:
            target_zone_id = zone["zoneId"]
            break

    if not target_zone_id:
        print("[좌석] 선택 가능한 구역 없음")
        return None

    print(f"[좌석] 구역별 조회: zoneId={target_zone_id}")
    res2 = session.get(
        f"{BASE_URL}/api/games/{game_id}/seats"
        f"?zoneId={target_zone_id}",
        headers={"Authorization": f"Bearer {token}"},
        timeout_seconds=300
    )
    if res2.status_code == 200:
        zones2 = res2.json()["data"]["zones"]
        for zone in zones2:
            available = [
                s for s in zone.get("seats", [])
                if s["status"] == "AVAILABLE"
            ]
            if available:
                seat_id = available[0]["seatId"]
                print(f"[좌석] 선택: {zone['zoneName']} "
                      f"seatId={seat_id}")
                return seat_id

    print("[좌석] 선택 가능한 좌석 없음")
    return None

def exit_existing_queue():
    res = session.get(
        f"{BASE_URL}/api/queue/my/{game_id}",
        headers={"Authorization": f"Bearer {token}"}
    )
    if res.status_code == 200:
        queue_token = res.json().get("data")
        if queue_token:
                    session.delete(
                        f"{BASE_URL}/api/queue/exit/{queue_token}",
                        headers={"Authorization": f"Bearer {token}"}
                    )
                    print("[대기열] 기존 대기열 나가기 완료")

def lock_seat(seat_id):
    start = time.time()
    res = session.post(
        f"{BASE_URL}/api/games/{game_id}/seats/lock",
        json={"seatIds": [seat_id]},
        headers={"Authorization": f"Bearer {token}"}
    )
    elapsed = time.time() - start
    response_times.append(elapsed)
    print(f"[선점 시도] 응답={res.status_code} 소요={elapsed:.3f}초")
    if res.status_code == 200:
        lock_token = res.json()["data"]["lockToken"]
        print(f"[선점 성공] lockToken={lock_token[:20]}...")
        return lock_token
    return None

def pay(lock_token):
    # 결제 준비
    start = time.time()
    prep = session.post(
        f"{BASE_URL}/api/payments/prepare",
        json={
            "lockToken": lock_token,
            "paymentMethod": "CARD"
        },
        headers={"Authorization": f"Bearer {token}"}
    )
    elapsed = time.time() - start
    response_times.append(elapsed)
    print(f"[결제 준비] 응답={prep.status_code} 소요={elapsed:.3f}초")

    if prep.status_code != 200:
        print(f"[결제 준비 실패] {prep.text}")
        return False

    order_id = prep.json()["data"]["orderId"]

    # 결제 승인
    start = time.time()
    confirm = session.post(
        f"{BASE_URL}/api/payments/confirm",
        json={
            "orderId": order_id,
            "pgPaymentId": order_id
        },
        headers={"Authorization": f"Bearer {token}"}
    )
    elapsed = time.time() - start
    response_times.append(elapsed)
    print(f"[결제 승인] 응답={confirm.status_code} 소요={elapsed:.3f}초")

    # 예매 확정
    start = time.time()
    reserve = session.post(
        f"{BASE_URL}/api/reservations",
        json={
            "lockToken": lock_token,
            "gameId": game_id
        },
        headers={"Authorization": f"Bearer {token}"}
    )
    elapsed = time.time() - start
    response_times.append(elapsed)
    print(f"[예매 확정] 응답={reserve.status_code} 소요={elapsed:.3f}초")
    return reserve.status_code == 200

if __name__ == "__main__":
    login()
    get_game()

    exit_existing_queue()

    queue_token = enter_queue()
    if not queue_token:
        exit(1)

    if not wait_for_allowed(queue_token):
        exit(1)

    seat_id = get_seat()
    if not seat_id:
        exit(1)

    lock_token = lock_seat(seat_id)
    if not lock_token:
        exit(1)

    success = pay(lock_token)
    if success:
        print("\n[예매 완료] ✅")
    else:
        print("\n[예매 실패] ❌")

    if response_times:
        avg = sum(response_times) / len(response_times)
        print(f"\n[결과 요약]")
        print(f"  TLS 클라이언트: {client_id}")
        print(f"  요청 횟수: {len(response_times)}회")
        print(f"  평균 응답: {avg:.3f}초")
        print(f"  최소 응답: {min(response_times):.3f}초")
        print(f"  최대 응답: {max(response_times):.3f}초")