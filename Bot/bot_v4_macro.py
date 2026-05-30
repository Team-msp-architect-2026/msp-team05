"""
Bot v4 — 좌석 선점 + 결제 + 예매 완료 전체 흐름
회원가입 → 로그인 → 좌석조회 → 좌석선점 → 결제준비 → 결제확정 → 예매완료
"""

import requests
import time
import random
import sys
from headers import get_random_profile

#BASE    = "http://localhost:8080"
BASE_URL = "http://localhost:8080"

#BOT_EMAIL    = f"botv6_{int(time.time())}@test.com"
#BOT_PASSWD   = "botv6pass1"
#BOT_USERNAME = "botv6user"
#BOT_PHONE    = "010-8888-1234"
EMAIL    = input("이메일 입력: ")
PASSWD = input("비밀번호 입력: ")

session = requests.Session()

"""
# ── 1. 회원가입 ───────────────────────────────────
def signup():
    headers = get_random_profile()
    headers["Content-Type"] = "application/json"
    res = session.post(f"{BASE}/api/auth/signup", json={
        "email":    BOT_EMAIL,
        "passwd":   BOT_PASSWD,
        "username": BOT_USERNAME,
        "phonenum": BOT_PHONE,
    }, headers=headers)
    if res.status_code == 201:
        print(f"[1/6] 회원가입 완료 | {BOT_EMAIL}")
    else:
        print(f"[1/6] 회원가입 실패: {res.json().get('message')}")
"""

# ── 1. 로그인 ──────────────────────────────────────────────
def login():
    headers = get_random_profile()
    headers["Content-Type"] = "application/json"
    res = session.post(f"{BASE_URL}/api/auth/login", json={
        "email": EMAIL,
        "passwd": PASSWD,
    }, headers=headers)
    data = res.json()
    if data.get("success"):
        token = data["data"]["accessToken"]
        print(f"[로그인 성공] UA: {headers['User-Agent'][:40]}...")
        return token
    else:
        print(f"[로그인 실패] {data.get('message')}")
        sys.exit(1)

"""
# ── 2. 로그인 ─────────────────────────────────────
def login():
    headers = get_random_profile()
    headers["Content-Type"] = "application/json"
    res = session.post(f"{BASE}/api/auth/login", json={
        "email":  BOT_EMAIL,
        "passwd": BOT_PASSWD,
    }, headers=headers)
    data = res.json()
    if data.get("success"):
        token = data["data"]["accessToken"]
        print(f"[2/6] 로그인 성공 | UA: {headers['User-Agent'][:40]}...")
        return token
    else:
        print(f"[2/6] 로그인 실패: {data.get('message')}")
        sys.exit(1)
"""

# ── 3. 경기 목록 조회 ─────────────────────
# API로 동적 조회로 변경
def get_game_id(token):
    headers = get_random_profile()
    headers["Authorization"] = f"Bearer {token}"
    res = session.get(f"{BASE_URL}/api/games", headers=headers)
    games = res.json().get("data", {}).get("content", [])
    for game in games:
        if game.get("status") == "ON_SALE":
            game_id = game["gameId"]
            print(f"[경기] {game['homeTeam']['name']} vs "
                  f"{game['awayTeam']['name']} | {game_id}")
            return game_id
    print("[경기] ON_SALE 경기 없음")
    sys.exit(1)

# ── 4. 좌석 조회 ──────────────────────────────────
def get_available_seat(token, game_id):
    headers = get_random_profile()
    headers["Authorization"] = f"Bearer {token}"
    res = session.get(f"{BASE_URL}/api/games/{game_id}/seats", headers=headers)
    zones = res.json().get("data", {}).get("zones", [])
    for zone in zones:
        for seat in zone.get("seats", []):
            if seat.get("status") == "AVAILABLE":
                print(f"[3/6] 좌석 확인 | {zone['zoneName']} {seat['rowNum']}열 {seat['number']}번 ({zone['price']:,}원)")
                return seat["seatId"], zone["zoneName"], zone["price"]
    print("[3/6] 예약 가능한 좌석 없음")
    sys.exit(1)


# ── 5. 좌석 선점 (Lock) ───────────────────────────
def lock_seat(token, seat_id, game_id):
    headers = get_random_profile()
    headers["Authorization"] = f"Bearer {token}"
    headers["Content-Type"] = "application/json"
    res = session.post(f"{BASE_URL}/api/games/{game_id}/seats/lock",
        json={"seatIds": [seat_id]}, headers=headers)
    data = res.json()
    if res.status_code == 200:
        lock_token = data["data"]["lockToken"]
        expires_at = data["data"]["expiresAt"]
        amount     = data["data"]["totalAmount"]
        print(f"[4/6] 좌석 선점 성공 | lockToken={lock_token[:8]}... | 만료={expires_at} | 금액={amount:,}원")
        return lock_token, amount
    else:
        print(f"[4/6] 좌석 선점 실패: {res.status_code} | {data.get('message')}")
        sys.exit(1)


# ── 6. 결제 준비 (Prepare) ────────────────────────
def prepare_payment(token, lock_token):
    headers = get_random_profile()
    headers["Authorization"] = f"Bearer {token}"
    headers["Content-Type"] = "application/json"
    res = session.post(f"{BASE_URL}/api/payments/prepare", json={
        "lockToken":     lock_token,
        "paymentMethod": "CARD",
    }, headers=headers)
    data = res.json()
    if res.status_code == 200:
        order_id = data["data"]["orderId"]
        amount   = data["data"]["amount"]
        status   = data["data"]["status"]
        print(f"[5/6] 결제 준비 완료 | orderId={order_id[:8]}... | 금액={amount:,}원 | 상태={status}")
        return order_id
    else:
        print(f"[5/6] 결제 준비 실패: {res.status_code} | {data.get('message')}")
        sys.exit(1)


# ── 7. 결제 확정 (Confirm) ────────────────────────
def confirm_payment(token, order_id):
    headers = get_random_profile()
    headers["Authorization"] = f"Bearer {token}"
    headers["Content-Type"] = "application/json"
    res = session.post(f"{BASE_URL}/api/payments/confirm", json={
        "orderId":     order_id,
        "pgPaymentId": order_id,
    }, headers=headers)
    if res.status_code == 200:
        try:
            data = res.json()
            status = data.get("data", {}).get("status", "DONE")
        except Exception:
            status = "DONE"
        print(f"[6/7] 결제 확정 완료 | orderId={order_id[:8]}... | 상태={status}")
        return True
    else:
        print(f"[6/7] 결제 확정 실패: {res.status_code} | {res.text[:100]}")
        return False


# ── 8. 예매 완료 (Reservation) ────────────────────
def create_reservation(token, lock_token, game_id):
    headers = get_random_profile()
    headers["Authorization"] = f"Bearer {token}"
    headers["Content-Type"] = "application/json"
    res = session.post(f"{BASE_URL}/api/reservations", json={
        "lockToken": lock_token,
        "gameId":    game_id,
    }, headers=headers)
    data = res.json()
    if res.status_code == 200:
        reservation = data["data"]
        print(f"[7/7] 예매 완료! | reservationId={str(reservation.get('reservationId', ''))[:8]}...")
        print(f"      좌석: {reservation.get('seatInfo', '')} | 상태: RESERVED")
        return True
    else:
        print(f"[7/7] 예매 실패: {res.status_code} | {data.get('message')}")
        return False


# ── 메인 ──────────────────────────────────────────
def run():
    print("=" * 55)
    print("Bot v4 — 좌석 선점 + 결제 + 예매 완료 전체 흐름")
    print("=" * 55)

    #signup()
    #time.sleep(random.uniform(0.2, 0.5))

    token = login()
    time.sleep(random.uniform(0.2, 0.5))

    # GAME_ID 동적 조회 추가
    game_id = get_game_id(token)
    time.sleep(random.uniform(0.2, 0.5))

    seat_id, zone_name, price = get_available_seat(token, game_id)
    time.sleep(random.uniform(0.2, 0.5))

    lock_token, amount = lock_seat(token, seat_id, game_id)
    time.sleep(random.uniform(0.2, 0.5))

    order_id = prepare_payment(token, lock_token)
    time.sleep(random.uniform(0.2, 0.5))

    confirmed = confirm_payment(token, order_id)
    if not confirmed:
        sys.exit(1)
    time.sleep(random.uniform(0.2, 0.5))

    create_reservation(token, lock_token, game_id)

    print("-" * 55)
    #print(f"[완료] 봇 예매 성공 | 계정: {BOT_EMAIL} | 금액: {amount:,}원")
    print(f"[완료] 봇 예매 성공 | 계정: {EMAIL} | 금액: {amount:,}원")

if __name__ == "__main__":
    run()
