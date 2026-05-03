package com.kky.ticketing.domain.game.controller;

import com.kky.ticketing.domain.game.dto.GameDetailResponse;
import com.kky.ticketing.domain.game.dto.GameListResponse;
import com.kky.ticketing.domain.game.service.GameService;
import com.kky.ticketing.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Games", description = "경기 목록 및 상세 조회 API")
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // ─────────────────────────────────────────
    // GET /api/games?date=2025-04-28&teamId=xxx&page=0&size=10
    // 경기 목록 조회 (메인화면)
    // ─────────────────────────────────────────
    @Operation(
        summary = "경기 목록 조회",
        description = "날짜/팀/구장 필터와 페이지네이션을 지원합니다. (Public)"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GameListResponse>>> getGames(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String teamId,
            @RequestParam(required = false) String stadiumId,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {

        Page<GameListResponse> response =
                gameService.getGames(date, teamId, stadiumId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ─────────────────────────────────────────
    // GET /api/games/{gameId}
    // 경기 상세 조회
    // ─────────────────────────────────────────
    @Operation(
        summary = "경기 상세 조회",
        description = "경기 상세 정보와 구역별 잔여 좌석을 반환합니다. (Public)"
    )
    @GetMapping("/{gameId}")
    public ResponseEntity<ApiResponse<GameDetailResponse>> getGame(
            @PathVariable String gameId) {

        GameDetailResponse response = gameService.getGame(gameId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
