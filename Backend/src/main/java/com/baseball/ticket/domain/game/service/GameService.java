package com.baseball.ticket.domain.game.service;

import com.baseball.ticket.domain.game.dto.GameDetailResponse;
import com.baseball.ticket.domain.game.dto.GameListResponse;
import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.repository.GameRepository;
import com.baseball.ticket.domain.seat.repository.SeatRepository;
import com.baseball.ticket.domain.seat.repository.SeatZoneRepository;
import com.baseball.ticket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final SeatZoneRepository seatZoneRepository;
    private final SeatRepository seatRepository;

    // ─────────────────────────────────────────
    // 경기 목록 조회 (메인화면)
    // GET /api/games
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<GameListResponse> getGames(
            String date,
            String teamId,
            String stadiumId,
            Pageable pageable) {

        LocalDate localDate = (date != null) ? LocalDate.parse(date) : null;

        Page<Game> games = gameRepository.findGamesWithFilter(
                localDate, teamId, stadiumId, pageable);

        return games.map(game -> {
            // AVAILABLE 좌석 수로 계산
            int remaining = seatRepository
                    .countAvailableByStadiumId(game.getStadium().getId());
            return GameListResponse.from(game, remaining);
        });
    }

    // ─────────────────────────────────────────
    // 경기 상세 조회
    // GET /api/games/{gameId}
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public GameDetailResponse getGame(String gameId) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", 404));

        // 구역별 잔여 좌석 목록
        List<GameDetailResponse.ZoneInfo> zoneInfos = seatZoneRepository
                .findByStadiumId(game.getStadium().getId())
                .stream()
                .map(zone -> GameDetailResponse.ZoneInfo.builder()
                        .zoneId(zone.getId())
                        .zoneName(zone.getZoneName())
                        .price(zone.getPrice())
                        .remainingSeats(zone.getTotalSeats())
                        .build())
                .collect(Collectors.toList());

        return GameDetailResponse.from(game, zoneInfos);
    }
}
