"""
1단계 봇 - Python requests HTTP 봇
WAF 탐지가 가장 쉬운 유형. 브라우저 없이 API를 직접 호출.
User-Agent, 헤더 조작이 쉬워 WAF Bot Control의 Before 기준점으로 활용.
"""
import requests
import argparse
import time
import csv
import os
from datetime import datetime

BASE_URL = "http://localhost:8080"
LOG_FILE = "bot_v1_results.csv"


class HTTPTicketingBot:
    def __init__(self, email: str, password: str, verbose: bool = True):
        self.email = email
        self.password = password
        self.verbose = verbose
        self.session = requests.Session()
        self.results = []

    def _log(self, msg: str):
        if self.verbose:
            print(f"[HTTP Bot] {msg}")

    def _record(self, endpoint: str, status_code: int, note: str = ""):
        entry = {
            "timestamp": datetime.now().isoformat(),
            "endpoint": endpoint,
            "status_code": status_code,
            "note": note,
        }
        self.results.append(entry)
        self._log(f"{endpoint} → {status_code} {note}")

    def _save_results(self):
        if not self.results:
            return
        file_exists = os.path.exists(LOG_FILE)
        with open(LOG_FILE, "a", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=["timestamp", "endpoint", "status_code", "note"])
            if not file_exists:
                writer.writeheader()
            writer.writerows(self.results)
        self._log(f"결과 저장 완료: {LOG_FILE} ({len(self.results)}건)")

    def _request(self, method: str, path: str, **kwargs):
        url = f"{BASE_URL}{path}"
        try:
            res = self.session.request(method, url, **kwargs)
            self._record(path, res.status_code)
            return res
        except requests.exceptions.RequestException as e:
            self._record(path, 0, str(e))
            raise

    def login(self) -> str:
        res = self._request("POST", "/api/auth/login", json={
            "email": self.email,
            "passwd": self.password,
        })
        res.raise_for_status()
        token = res.json()["data"]["accessToken"]
        self.session.headers.update({"Authorization": f"Bearer {token}"})
        self._log(f"로그인 성공: {self.email}")
        return token

    def get_games(self) -> list:
        res = self._request("GET", "/api/games")
        res.raise_for_status()
        return res.json()["data"]["content"]

    def get_seats(self, game_id: str) -> list:
        res = self._request("GET", f"/api/games/{game_id}/seats")
        res.raise_for_status()
        zones = res.json()["data"]["zones"]
        all_seats = []
        for zone in zones:
            for seat in zone.get("seats", []):
                seat["zoneId"] = zone["zoneId"]
                all_seats.append(seat)
        return all_seats

    def hold_seats(self, game_id: str, seat_ids: list) -> str:
        """좌석 선점 후 lockToken 반환"""
        res = self._request("POST", f"/api/games/{game_id}/seats/lock", json={"seatIds": seat_ids})
        res.raise_for_status()
        return res.json()["data"]["lockToken"]

    def reserve(self, game_id: str, lock_token: str) -> dict:
        res = self._request("POST", "/api/reservations", json={
            "gameId": game_id,
            "lockToken": lock_token,
        })
        res.raise_for_status()
        return res.json()

    def run_once(self, num_seats: int = 1) -> dict | None:
        self.login()

        games = self.get_games()
        on_sale = [g for g in games if g.get("status") == "ON_SALE"]
        if not on_sale:
            self._log("예매 가능한 경기 없음")
            return None
        game = on_sale[0]
        game_id = game["gameId"]
        self._log(f"경기 선택: (id={game_id})")

        seats = self.get_seats(game_id)
        available_seats = [s for s in seats if s.get("status") == "AVAILABLE"]
        if len(available_seats) < num_seats:
            self._log(f"잔여 좌석 부족: {len(available_seats)}개")
            return None
        target_ids = [s["seatId"] for s in available_seats[:num_seats]]
        self._log(f"좌석 점유 시도: {target_ids}")

        lock_token = self.hold_seats(game_id, target_ids)
        self._log(f"좌석 점유 완료 (lockToken={lock_token})")

        result = self.reserve(game_id, lock_token)
        self._log(f"예매 성공: reservationId={result['data'].get('reservationId')}")
        return result

    def run(self, num_seats: int = 1, repeat: int = 1, interval: float = 1.0):
        self._log(f"반복 실행 시작: {repeat}회, 간격 {interval}초")
        success = 0
        blocked = 0

        for i in range(repeat):
            self._log(f"--- 시도 {i + 1}/{repeat} ---")
            try:
                result = self.run_once(num_seats)
                if result:
                    success += 1
            except requests.exceptions.HTTPError as e:
                status = e.response.status_code if e.response else 0
                if status in (403, 405, 429):
                    blocked += 1
                    self._log(f"WAF 차단 감지: {status}")
            except Exception as e:
                self._log(f"오류: {e}")

            if i < repeat - 1:
                time.sleep(interval)

        self._log(f"완료 — 성공: {success}, 차단: {blocked}, 전체: {repeat}")
        self._save_results()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="1단계 HTTP 봇")
    parser.add_argument("--email", default="bot@test.com")
    parser.add_argument("--password", default="test1234!!")
    parser.add_argument("--seats", type=int, default=1, help="예매 좌석 수")
    parser.add_argument("--repeat", type=int, default=1, help="반복 횟수")
    parser.add_argument("--interval", type=float, default=1.0, help="요청 간격 (초)")
    parser.add_argument("--url", default=BASE_URL, help="대상 서버 URL")
    args = parser.parse_args()

    BASE_URL = args.url

    bot = HTTPTicketingBot(email=args.email, password=args.password)
    bot.run(num_seats=args.seats, repeat=args.repeat, interval=args.interval)