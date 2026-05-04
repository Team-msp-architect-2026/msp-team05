"""
3단계 봇 - Python Playwright + Stealth 봇
playwright-stealth로 navigator.webdriver, 브라우저 fingerprint를 숨겨
WAF Bot Control의 JS 챌린지 및 브라우저 자동화 탐지를 우회 시도.
프록시 옵션으로 IP 기반 차단도 우회 가능.
"""
import argparse
import asyncio
import sys

from playwright.async_api import async_playwright, Page
from playwright_stealth import stealth_async

FRONTEND_URL = "http://localhost:3000"


class PlaywrightTicketingBot:
    def __init__(self, email: str, password: str, headless: bool = False, proxy: str | None = None):
        self.email = email
        self.password = password
        self.headless = headless
        # proxy 형식: "http://host:port" 또는 "http://user:pass@host:port"
        self.proxy = proxy

    def _log(self, msg: str):
        print(f"[Playwright Bot] {msg}")

    async def _setup_page(self, playwright) -> tuple:
        launch_kwargs = {"headless": self.headless}
        if self.proxy:
            launch_kwargs["proxy"] = {"server": self.proxy}

        browser = await playwright.chromium.launch(**launch_kwargs)
        context = await browser.new_context(
            # 실제 브라우저처럼 보이게 viewport, locale 설정
            viewport={"width": 1280, "height": 800},
            locale="ko-KR",
            timezone_id="Asia/Seoul",
        )
        page = await context.new_page()

        # stealth 적용: navigator.webdriver 숨기기, plugins 위장 등
        await stealth_async(page)
        self._log("Stealth 모드 적용 완료")
        return browser, page

    async def login(self, page: Page):
        await page.goto(f"{FRONTEND_URL}/login")
        self._log("로그인 페이지 이동")

        await page.fill("input[type='email']", self.email)
        await page.fill("input[type='password']", self.password)
        await page.click("button[type='submit']")

        await page.wait_for_url(f"{FRONTEND_URL}/", timeout=10000)
        self._log(f"로그인 성공: {self.email}")

    async def select_game(self, page: Page) -> str:
        await page.goto(FRONTEND_URL)
        await page.wait_for_selector("a:has-text('예매하기')", timeout=10000)

        book_link = page.locator("a:has-text('예매하기')").first
        href = await book_link.get_attribute("href")
        game_id = href.rstrip("/").split("/")[-1]
        await book_link.click()
        self._log(f"경기 선택: gameId={game_id}")
        return game_id

    async def select_section(self, page: Page):
        await page.wait_for_selector("h2:has-text('구역 선택')", timeout=10000)
        section_btn = page.locator("button:not([disabled])").filter(has_text="원").first
        section_name = await section_btn.locator("div.font-medium").text_content()
        await section_btn.click()
        self._log(f"구역 선택: {section_name}")
        await page.wait_for_timeout(500)

    async def select_seats(self, page: Page, num_seats: int = 1):
        await page.wait_for_selector("h2:has-text('좌석 선택')", timeout=10000)
        # bg-green-100 = AVAILABLE 좌석
        available = page.locator("button.bg-green-100:not([disabled])")
        count = min(num_seats, await available.count())
        for i in range(count):
            await available.nth(i).click()
            await page.wait_for_timeout(100)
        self._log(f"{count}석 선택 완료")

    async def confirm_booking(self, page: Page):
        book_btn = page.locator("button:has-text('예매하기')").last
        await book_btn.wait_for(state="visible", timeout=10000)
        await book_btn.click()
        self._log("예매하기 버튼 클릭")

        await page.wait_for_url("**/checkout", timeout=10000)
        self._log("예매 완료 — 결제 페이지 도달")

    async def run(self, num_seats: int = 1):
        async with async_playwright() as playwright:
            browser, page = await self._setup_page(playwright)
            try:
                await self.login(page)
                await self.select_game(page)
                await self.select_section(page)
                await self.select_seats(page, num_seats)
                await self.confirm_booking(page)
            except Exception as e:
                self._log(f"오류 발생: {e}")
                raise
            finally:
                await browser.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="3단계 Playwright + Stealth 봇")
    parser.add_argument("--email", default="bot@test.com")
    parser.add_argument("--password", default="password123!")
    parser.add_argument("--seats", type=int, default=1)
    parser.add_argument("--headless", action="store_true")
    parser.add_argument("--proxy", default=None, help="프록시 주소 (예: http://host:port)")
    args = parser.parse_args()

    bot = PlaywrightTicketingBot(
        email=args.email,
        password=args.password,
        headless=args.headless,
        proxy=args.proxy,
    )
    try:
        asyncio.run(bot.run(num_seats=args.seats))
    except Exception:
        sys.exit(1)
