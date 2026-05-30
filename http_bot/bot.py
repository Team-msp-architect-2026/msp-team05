"""
1단계 봇 - Python requests HTTP 봇
WAF 탐지가 가장 쉬운 유형. 브라우저 없이 API를 직접 호출.
User-Agent, 헤더 조작이 쉬워 WAF Bot Control의 Before 기준점으로 활용.
"""
import requests
import argparse
import sys

BASE_URL = "http://localhost:8080"


class HTTPTicketingBot:
    def __init__(self, email: str, password: str, verbose: bool = True):
        self.email = email
        self.password = password
        self.verbose = verbose
        self.session = requests.Session()
        # WAF 탐지 포인트: requests 기본 User-Agent ("python-requests/x.x.x") 그대로 사용
        # 실제 브라우저처럼 보이게 하려면 아래 주석을 해제
        # self.session.headers.update({"User-Agent": "Mozilla/5.0 ..."})

    def _log(self, msg: str):
        if self.verbose:
            print(f"[HTTP Bot] {msg}")

    def login(self) -> str:
        res = self.session.post(f"{BASE_URL}/api/auth/login", json={
            "email": self.email,
            "password": self.password,
        })
        res.raise_for_status()
        token = res.json()["accessToken"]
        self.session.headers.update({"Authorization": f"Bearer {token}"})
        self._log(f"로그인 성공: {self.email}")
        return token

    def get_games(self) -> list:
        res = self.session.get(f"{BASE_URL}/api/games")
        res.raise_for_status()
        return res.json()

    def get_sections(self, game_id: int) -> list:
        res = self.session.get(f"{BASE_URL}/api/games/{game_id}/sections")
        res.raise_for_status()
        return res.json()

    def get_seats(self, section_id: int) -> list:
        res = self.session.get(f"{BASE_URL}/api/games/sections/{section_id}/seats")
        res.raise_for_status()
        return res.json()

    def hold_seats(self, seat_ids: list) -> bool:
        res = self.session.post(f"{BASE_URL}/api/seats/hold", json={"seatIds": seat_ids})
        res.raise_for_status()
        return True

    def reserve(self, game_id: int, seat_ids: list) -> dict:
        res = self.session.post(f"{BASE_URL}/api/reservations", json={
            "gameId": game_id,
            "seatIds": seat_ids,
        })
        res.raise_for_status()
        return res.json()

    def run(self, num_seats: int = 1) -> dict | None:
        self.login()

        games = self.get_games()
        on_sale = [g for g in games if g.get("status") == "ON_SALE" and g.get("totalAvailableSeats", 0) > 0]
        if not on_sale:
            self._log("예매 가능한 경기 없음")
            return None
        game = on_sale[0]
        game_id = game["id"]
        self._log(f"경기 선택: {game.get('homeTeam', {}).get('shortName')} vs {game.get('awayTeam', {}).get('shortName')} (id={game_id})")

        sections = self.get_sections(game_id)
        available_sections = [s for s in sections if s.get("availableSeats", 0) >= num_seats]
        if not available_sections:
            self._log("잔여 좌석이 있는 구역 없음")
            return None
        section = available_sections[0]
        section_id = section["id"]
        self._log(f"구역 선택: {section.get('name')} (id={section_id})")

        seats = self.get_seats(section_id)
        available_seats = [s for s in seats if s.get("status") == "AVAILABLE"]
        if len(available_seats) < num_seats:
            self._log(f"잔여 좌석 부족: {len(available_seats)}개")
            return None
        target_ids = [s["id"] for s in available_seats[:num_seats]]
        self._log(f"좌석 점유 시도: {target_ids}")

        self.hold_seats(target_ids)
        self._log("좌석 점유 완료 (5분 유지)")

        result = self.reserve(game_id, target_ids)
        self._log(f"예매 성공: reservationId={result.get('reservationId')}")
        return result


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="1단계 HTTP 봇")
    parser.add_argument("--email", default="bot@test.com")
    parser.add_argument("--password", default="password123!")
    parser.add_argument("--seats", type=int, default=1)
    args = parser.parse_args()

    bot = HTTPTicketingBot(email=args.email, password=args.password)
    result = bot.run(num_seats=args.seats)
    if result is None:
        sys.exit(1)
