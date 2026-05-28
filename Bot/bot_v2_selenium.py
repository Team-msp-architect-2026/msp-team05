from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
import time

BASE_URL = "http://localhost:5173"
EMAIL    = input("이메일 입력: ")
PASSWD   = input("비밀번호 입력: ")
HEADLESS = input("Headless 모드? (y/n): ").strip().lower() == "y"

response_times = []

def run_bot():
    options = Options()
    if HEADLESS:
        options.add_argument("--headless")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        print("[모드] Headless")
    else:
        print("[모드] Non-headless")

    driver = webdriver.Chrome(
        service=Service(ChromeDriverManager().install()),
        options=options
    )
    wait = WebDriverWait(driver, 15)

    try:
        # 1. 로그인
        print(f"[접속] {BASE_URL}/login")
        driver.get(f"{BASE_URL}/login")
        time.sleep(1)

        wait.until(EC.presence_of_element_located(
            (By.NAME, "email")
        )).send_keys(EMAIL)
        driver.find_element(By.NAME, "passwd").send_keys(PASSWD)

        start = time.time()
        driver.find_element(
            By.CSS_SELECTOR, "button[type='submit']"
        ).click()
        elapsed = time.time() - start
        response_times.append(elapsed)
        print(f"[로그인 시도] 소요={elapsed:.3f}초")

        # 2. 메인 페이지 이동 대기
        wait.until(EC.url_changes(f"{BASE_URL}/login"))
        print(f"[현재 URL] {driver.current_url}")
        time.sleep(2)

        # 3. 예매하기 버튼 클릭
        buy_btn = wait.until(EC.element_to_be_clickable(
            (By.XPATH, "//button[contains(text(),'예매하기')]")
        ))
        buy_btn.click()
        print("[경기 선택] 예매하기 버튼 클릭")
        time.sleep(2)
        print(f"[경기 URL] {driver.current_url}")

        # 4. 좌석 선택 버튼 클릭
        seat_btn = wait.until(EC.element_to_be_clickable(
            (By.XPATH, "//button[contains(text(),'좌석 선택')]")
        ))
        start = time.time()
        seat_btn.click()
        elapsed = time.time() - start
        response_times.append(elapsed)
        print(f"[좌석 선택] 버튼 클릭 소요={elapsed:.3f}초")
        time.sleep(2)
        print(f"[대기열 URL] {driver.current_url}")

        # 5. 대기열 ALLOWED 대기 (최대 60초)
        print("[대기열] ALLOWED 상태 대기 중... (최대 60초)")
        wait_seats = WebDriverWait(driver, 60)
        wait_seats.until(EC.url_contains("/seats"))
        print(f"[좌석 페이지] {driver.current_url}")

        # 6. Spinner 사라질 때까지 대기
        # SeatSelectPage loadingZones: true → Spinner 표시
        # API 응답 완료 후 → Spinner 사라짐
        # 좌석 수가 많아서 API 응답 시간이 길어요.
        try:
            WebDriverWait(driver, 120).until(
                EC.invisibility_of_element_located(
                    (By.CSS_SELECTOR, "div.animate-spin")
                )
            )
            print("[구역] 로딩 완료")
        except:
            print("[구역] 로딩 타임아웃 (계속 진행)")

        time.sleep(1)


        # 7. 구역 div 클릭
        zone_items = driver.find_elements(
            By.XPATH,
            "//div[contains(@class,'cursor-pointer') "
            "and contains(@class,'border-b')]"
        )
        print(f"[구역] 발견된 구역 수: {len(zone_items)}")

        if zone_items:
            driver.execute_script(
                "arguments[0].click()", zone_items[0]
            )
            print("[구역 선택] 첫 번째 구역 클릭")

            # ── 추가: 좌석 로딩 대기 ──────────────
            # 구역 클릭 후 좌석 API 호출 완료까지 대기
            try:
                WebDriverWait(driver, 30).until(
                    EC.presence_of_element_located(
                        (By.CSS_SELECTOR,
                            "button.bg-white.border.border-gray-300")
                    )
                )
                print("[좌석] 로딩 완료")
            except:
                print("[좌석] 로딩 타임아웃")
            time.sleep(1)
            # ─────────────────────────────────────
        else:
            print("[구역] 선택 가능한 구역 없음")


        # 8. 좌석 선택
        seats = driver.find_elements(
            By.CSS_SELECTOR,
            "button.bg-white.border.border-gray-300"
        )
        print(f"[좌석] 발견된 좌석 수: {len(seats)}")

        if seats:
            start = time.time()
            driver.execute_script(
                "arguments[0].click()", seats[0]
            )
            elapsed = time.time() - start
            response_times.append(elapsed)
            print(f"[좌석 선택] 소요={elapsed:.3f}초")
            time.sleep(1)
        else:
            print("[좌석] 선택 가능한 좌석 없음")

        # 9. 선택 완료 버튼 클릭
        try:
            lock_btn = wait.until(
                EC.presence_of_element_located(
                    (By.XPATH,
                     "//button[contains(text(),'선택 완료')]")
                )
            )
            start = time.time()
            driver.execute_script(
                "arguments[0].click()", lock_btn
            )
            elapsed = time.time() - start
            response_times.append(elapsed)
            print(f"[선점 시도] 소요={elapsed:.3f}초")
            time.sleep(2)
            # ── 추가: alert 처리 ──────────────────
            try:
                alert = driver.switch_to.alert
                print(f"[Alert] {alert.text}")
                alert.accept()
            except:
                pass
            # ─────────────────────────────────────
            time.sleep(1)
        except Exception as e:
            print(f"[선점 에러] {e}")
            try:
                alert = driver.switch_to.alert
                alert.accept()
            except:
                pass

        # 10. /checkout 페이지 이동 대기
        try:
            WebDriverWait(driver, 15).until(
                EC.url_contains("/checkout")
            )
            print(f"[결제 페이지] {driver.current_url}")
            time.sleep(1)

            # 결제하기 버튼 클릭
            pay_btn = wait.until(EC.element_to_be_clickable(
                (By.XPATH,
                 "//button[contains(text(),'결제하기')]")
            ))
            start = time.time()
            pay_btn.click()
            elapsed = time.time() - start
            response_times.append(elapsed)
            print(f"[결제 시도] 소요={elapsed:.3f}초")
            time.sleep(3)

            # alert 처리 (예매 완료 or 실패)
            try:
                alert = driver.switch_to.alert
                print(f"[Alert] {alert.text}")
                alert.accept()
                print("[결제 완료]")
            except:
                pass

            print(f"[최종 URL] {driver.current_url}")

        except Exception as e:
            print(f"[결제 에러] {e}")
            try:
                alert = driver.switch_to.alert
                alert.accept()
            except:
                pass

        # 11. 스크린샷
        driver.save_screenshot("bot_v2_result.png")
        print("[스크린샷] bot_v2_result.png 저장")

    except Exception as e:
        print(f"[에러] {e}")
        driver.save_screenshot("bot_v2_error.png")

    finally:
        driver.quit()
        if response_times:
            avg = sum(response_times) / len(response_times)
            print(f"\n[결과 요약]")
            print(f"  요청 횟수: {len(response_times)}회")
            print(f"  평균 응답: {avg:.3f}초")
            print(f"  최소 응답: {min(response_times):.3f}초")
            print(f"  최대 응답: {max(response_times):.3f}초")

if __name__ == "__main__":
    run_bot()