package com.baseball.ticket.global.init;

import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.entity.Stadium;
import com.baseball.ticket.domain.game.entity.Team;
import com.baseball.ticket.domain.game.repository.GameRepository;
import com.baseball.ticket.domain.game.repository.StadiumRepository;
import com.baseball.ticket.domain.game.repository.TeamRepository;
import com.baseball.ticket.domain.seat.entity.Seat;
import com.baseball.ticket.domain.seat.entity.SeatZone;
import com.baseball.ticket.domain.seat.repository.SeatRepository;
import com.baseball.ticket.domain.seat.repository.SeatZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final SeatZoneRepository seatZoneRepository;
    private final SeatRepository seatRepository;
    private final GameRepository gameRepository;

    private record TeamDef(String name, String logoUrl) {}
    private record ZoneDef(String name, int price, int totalSeats) {}
    private record StadiumDef(String name, String address, List<ZoneDef> zones) {}
    private record GameDef(String homeTeam, String awayTeam, String stadium, LocalDateTime startTime, LocalDateTime ticketOpenTime) {}

    private static final List<TeamDef> TEAMS = List.of(
            new TeamDef("LG 트윈스",   ""),
            new TeamDef("두산 베어스",  ""),
            new TeamDef("키움 히어로즈",""),
            new TeamDef("SSG 랜더스",  ""),
            new TeamDef("KT 위즈",     ""),
            new TeamDef("한화 이글스",  ""),
            new TeamDef("KIA 타이거즈", ""),
            new TeamDef("삼성 라이온즈",""),
            new TeamDef("롯데 자이언츠",""),
            new TeamDef("NC 다이노스",  "")
    );

    private static final List<StadiumDef> STADIUMS = List.of(
            new StadiumDef("서울종합운동장 야구장", "서울특별시 송파구 올림픽로 25",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 6000),
                            new ZoneDef("3루 지정석",  15000, 6000),
                            new ZoneDef("중앙석",      25000, 4000),
                            new ZoneDef("외야 응원석", 11000, 8411)
                    )),
            new StadiumDef("고척스카이돔", "서울특별시 구로구 경인로 430",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 4000),
                            new ZoneDef("3루 지정석",  15000, 4000),
                            new ZoneDef("중앙석",      25000, 3000),
                            new ZoneDef("외야 응원석", 11000, 5000)
                    )),
            new StadiumDef("인천SSG랜더스필드", "인천광역시 미추홀구 매소홀로 618",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 5500),
                            new ZoneDef("3루 지정석",  15000, 5500),
                            new ZoneDef("중앙석",      25000, 4000),
                            new ZoneDef("외야 응원석", 11000, 8000)
                    )),
            new StadiumDef("수원KT위즈파크", "경기도 수원시 장안구 경수대로 893",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 5000),
                            new ZoneDef("3루 지정석",  15000, 5000),
                            new ZoneDef("중앙석",      25000, 3500),
                            new ZoneDef("외야 응원석", 11000, 6500)
                    )),
            new StadiumDef("대전한화생명볼파크", "대전광역시 중구 대종로 373",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 4200),
                            new ZoneDef("3루 지정석",  15000, 4200),
                            new ZoneDef("중앙석",      25000, 3100),
                            new ZoneDef("외야 응원석", 11000, 5500)
                    )),
            new StadiumDef("광주기아챔피언스필드", "광주광역시 북구 서림로 10",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 5000),
                            new ZoneDef("3루 지정석",  15000, 5000),
                            new ZoneDef("중앙석",      25000, 3500),
                            new ZoneDef("외야 응원석", 11000, 6500)
                    )),
            new StadiumDef("대구삼성라이온즈파크", "대구광역시 수성구 야구전설로 1",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 6000),
                            new ZoneDef("3루 지정석",  15000, 6000),
                            new ZoneDef("중앙석",      25000, 4000),
                            new ZoneDef("외야 응원석", 11000, 8000)
                    )),
            new StadiumDef("사직야구장", "부산광역시 동래구 사직로 45",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 6000),
                            new ZoneDef("3루 지정석",  15000, 6000),
                            new ZoneDef("중앙석",      25000, 4000),
                            new ZoneDef("외야 응원석", 11000, 8000)
                    )),
            new StadiumDef("창원NC파크", "경상남도 창원시 마산회원구 삼호로 63",
                    List.of(
                            new ZoneDef("1루 지정석",  15000, 4400),
                            new ZoneDef("3루 지정석",  15000, 4400),
                            new ZoneDef("중앙석",      25000, 3200),
                            new ZoneDef("외야 응원석", 11000, 5891)
                    )),
            new StadiumDef("울산문수야구장", "울산광역시 남구 문수로 44",
                    List.of(
                            new ZoneDef("1루 지정석",  12000, 3000),
                            new ZoneDef("3루 지정석",  12000, 3000),
                            new ZoneDef("중앙석",      20000, 2000),
                            new ZoneDef("외야 응원석",  9000, 4000)
                    ))
    );

    private static final List<GameDef> GAMES = List.of(
            new GameDef("LG 트윈스", "한화 이글스", "서울종합운동장 야구장",
                    LocalDateTime.of(2026, 6, 20, 18, 30),
                    LocalDateTime.of(2026, 6, 15, 10, 0)),
            new GameDef("두산 베어스", "키움 히어로즈", "서울종합운동장 야구장",
                    LocalDateTime.of(2026, 6, 21, 14, 0),
                    LocalDateTime.of(2026, 6, 15, 10, 0)),
            new GameDef("SSG 랜더스", "KT 위즈", "인천SSG랜더스필드",
                    LocalDateTime.of(2026, 6, 22, 18, 30),
                    LocalDateTime.of(2026, 6, 15, 10, 0))
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (TeamDef teamDef : TEAMS) {
            if (teamRepository.findByName(teamDef.name()).isPresent()) continue;
            teamRepository.save(Team.builder()
                    .name(teamDef.name())
                    .logoUrl(teamDef.logoUrl())
                    .build());
            log.info("팀 초기 데이터 생성 완료: {}", teamDef.name());
        }

        for (StadiumDef stadiumDef : STADIUMS) {
            if (stadiumRepository.findByName(stadiumDef.name()).isPresent()) continue;

            Stadium stadium = stadiumRepository.save(Stadium.builder()
                    .name(stadiumDef.name())
                    .address(stadiumDef.address())
                    .build());

            for (ZoneDef zoneDef : stadiumDef.zones()) {
                SeatZone zone = seatZoneRepository.save(SeatZone.builder()
                        .stadium(stadium)
                        .zoneName(zoneDef.name())
                        .price(zoneDef.price())
                        .totalSeats(zoneDef.totalSeats())
                        .build());

                int seatsPerRow = 10;
                int totalRows = (int) Math.ceil((double) zoneDef.totalSeats() / seatsPerRow);
                int count = 0;

                for (int r = 0; r < totalRows && count < zoneDef.totalSeats(); r++) {
                    String rowName = String.valueOf((char) ('A' + r));
                    for (int n = 1; n <= seatsPerRow && count < zoneDef.totalSeats(); n++) {
                        seatRepository.save(Seat.builder()
                                .zone(zone)
                                .rowNum(rowName)
                                .number(String.valueOf(n))
                                .status("AVAILABLE")
                                .build());
                        count++;
                    }
                }
            }
            log.info("구장 초기 데이터 생성 완료: {}", stadiumDef.name());
        }

        // 경기 데이터 삽입
        if (gameRepository.count() == 0) {
            for (GameDef gameDef : GAMES) {
                Team homeTeam = teamRepository.findByName(gameDef.homeTeam()).orElse(null);
                Team awayTeam = teamRepository.findByName(gameDef.awayTeam()).orElse(null);
                Stadium stadium = stadiumRepository.findByName(gameDef.stadium()).orElse(null);
                if (homeTeam == null || awayTeam == null || stadium == null) continue;
                gameRepository.save(Game.builder()
                        .homeTeam(homeTeam)
                        .awayTeam(awayTeam)
                        .stadium(stadium)
                        .startTime(gameDef.startTime())
                        .ticketOpenTime(gameDef.ticketOpenTime())
                        .status(Game.GameStatus.ON_SALE)
                        .build());
                log.info("경기 초기 데이터 생성 완료: {} vs {}", gameDef.homeTeam(), gameDef.awayTeam());
            }
        }
    }
}