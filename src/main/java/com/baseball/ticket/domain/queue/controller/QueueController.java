package com.baseball.ticket.domain.queue.controller;

import com.baseball.ticket.domain.queue.dto.QueueEnterRequest;
import com.baseball.ticket.domain.queue.dto.QueueEnterResponse;
import com.baseball.ticket.domain.queue.dto.QueueStatusResponse;
import com.baseball.ticket.domain.queue.service.QueueService;
import com.baseball.ticket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Queue", description = "대기열 API")
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    // POST /api/queue/enter
    @Operation(summary = "대기열 진입")
    @PostMapping("/enter")
    public ResponseEntity<ApiResponse<QueueEnterResponse>> enter(
            @Valid @RequestBody QueueEnterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 테스트용 — JWT 없을 때 임시 이메일 사용
        String email = userDetails != null
                ? userDetails.getUsername()
                : "test@test.com";

        QueueEnterResponse response =
                queueService.enter(request, email);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // GET /api/queue/status/{queueToken}
    @Operation(summary = "순번 조회")
    @GetMapping("/status/{queueToken}")
    public ResponseEntity<ApiResponse<QueueStatusResponse>> getStatus(
            @PathVariable String queueToken) {

        QueueStatusResponse response =
                queueService.getStatus(queueToken);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // DELETE /api/queue/exit/{queueToken}
    @Operation(summary = "대기열 이탈")
    @DeleteMapping("/exit/{queueToken}")
    public ResponseEntity<ApiResponse<Void>> exit(
            @PathVariable String queueToken) {

        queueService.exit(queueToken);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}