"""
2단계 봇 - Python Selenium 브라우저 자동화 봇
Node.js Puppeteer 코드를 Python Selenium으로 포팅.
실제 Chrome을 띄워 프론트엔드 UI를 통해 예매 진행.
navigator.webdriver 플래그가 노출되어 WAF Bot Control이 탐지 가능.
"""
import argparse
import sys
import time

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

FRONTEND_URL = "http://localhost:3000"
WAIT_TIMEOUT = 10


class SeleniumTicketingBot:
    def __init__(self, email: str, password: str, headless: bool = False):
        self.email = email
        self.password = password
        self.headless = headless
        self.driver: webdriver.Chrome | None = None
        self.wait: WebDriverWait | None = None

    def _log(self, msg: str):
        print(f"[Selenium Bot] {msg}")

    def start(self):
        options = Options()
        if self.headless:
            options.add_argument("--headless=new")
        # WAF 탐지 포인트: --disable-blink-features 없으면 navigator.webdriver=true 노출
        # 아래 주석을 해제하면 webdriver 플래그 숨기기 시도 가능 (완전 우회는 아님)
        # options.add_argument("--disable-blink-features=AutomationControlled")
        # options.add_experimental_option("excludeSwitches", ["enable-automation"])
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")

        self.driver = webdriver.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, WAIT_TIMEOUT)
        self._log("Chrome 실행 완료")

    def login(self):
        self.driver.get(f"{FRONTEND_URL}/login")
        self._log("로그인 페이지 이동")

        email_input = self.wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "input[type='email']")))
        email_input.clear()
        email_input.send_keys(self.email)

        pw_input = self.driver.find_element(By.CSS_SELECTOR, "input[type='password']")
        pw_input.clear()
        pw_input.send_keys(self.password)

        self.driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()

        # 로그인 성공 시 홈("/")으로 리다이렉트
        self.wait.until(EC.url_to_be(f"{FRONTEND_URL}/"))
        self._log(f"로그인 성공: {self.email}")

    def select_game(self) -> str:
        """ON_SALE 상태의 첫 번째 경기 예매하기 링크 클릭, game_id 반환"""
        self.driver.get(FRONTEND_URL)

        book_btn = self.wait.until(EC.element_to_be_clickable(
            (By.XPATH, "//a[normalize-space(text())='예매하기']")
        ))
        # href="/games/{gameId}" 에서 gameId 추출
        href = book_btn.get_attribute("href")
        game_id = href.rstrip("/").split("/")[-1]
        book_btn.click()
        self._log(f"경기 선택: gameId={game_id}")
        return game_id

    def select_section(self):
        """첫 번째 구역 버튼 클릭"""
        section_btn = self.wait.until(EC.element_to_be_clickable(
            (By.XPATH, "//h2[text()='구역 선택']/following-sibling::div//button[not(@disabled)]")
        ))
        section_name = section_btn.find_element(By.CSS_SELECTOR, "div.font-medium").text
        section_btn.click()
        self._log(f"구역 선택: {section_name}")
        time.sleep(0.5)

    def select_seats(self, num_seats: int = 1):
        """AVAILABLE 상태(green) 좌석 num_seats개 클릭"""
        self.wait.until(EC.presence_of_element_located(
            (By.XPATH, "//h2[contains(text(),'좌석 선택')]")
        ))
        # bg-green-100 클래스를 가진 활성 좌석 버튼
        available_seats = self.wait.until(EC.presence_of_all_elements_located(
            (By.XPATH, "//button[contains(@class,'bg-green-100') and not(@disabled)]")
        ))
        count = min(num_seats, len(available_seats))
        for i in range(count):
            available_seats[i].click()
            time.sleep(0.1)
        self._log(f"{count}석 선택 완료")

    def confirm_booking(self):
        """하단 '예매하기' 버튼 클릭"""
        book_btn = self.wait.until(EC.element_to_be_clickable(
            (By.XPATH, "//button[normalize-space(text())='예매하기']")
        ))
        book_btn.click()
        self._log("예매하기 버튼 클릭")

        # 결제 페이지("/checkout")로 이동 확인
        self.wait.until(EC.url_contains("/checkout"))
        self._log("예매 완료 — 결제 페이지 도달")

    def quit(self):
        if self.driver:
            self.driver.quit()

    def run(self, num_seats: int = 1):
        try:
            self.start()
            self.login()
            self.select_game()
            self.select_section()
            self.select_seats(num_seats)
            self.confirm_booking()
        except Exception as e:
            self._log(f"오류 발생: {e}")
            raise
        finally:
            self.quit()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="2단계 Selenium 봇")
    parser.add_argument("--email", default="bot@test.com")
    parser.add_argument("--password", default="password123!")
    parser.add_argument("--seats", type=int, default=1)
    parser.add_argument("--headless", action="store_true")
    args = parser.parse_args()

    bot = SeleniumTicketingBot(email=args.email, password=args.password, headless=args.headless)
    try:
        bot.run(num_seats=args.seats)
    except Exception:
        sys.exit(1)
