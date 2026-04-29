package com.kkybot.domain.admin;

import com.kkybot.common.ApiResponse;
import com.kkybot.domain.admin.dto.AdminRequest;
import com.kkybot.domain.admin.dto.AdminResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/teams")
    public ResponseEntity<ApiResponse<AdminResponse.TeamInfo>> createTeam(
            @Valid @RequestBody AdminRequest.TeamCreate req) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createTeam(req)));
    }

    @PostMapping("/stadiums")
    public ResponseEntity<ApiResponse<AdminResponse.StadiumInfo>> createStadium(
            @Valid @RequestBody AdminRequest.StadiumCreate req) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createStadium(req)));
    }

    @PostMapping("/games")
    public ResponseEntity<ApiResponse<AdminResponse.GameInfo>> createGame(
            @Valid @RequestBody AdminRequest.GameCreate req) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createGame(req)));
    }

    @PostMapping("/zones")
    public ResponseEntity<ApiResponse<AdminResponse.ZoneInfo>> createZone(
            @Valid @RequestBody AdminRequest.ZoneCreate req) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createZone(req)));
    }

    @PostMapping("/seats")
    public ResponseEntity<ApiResponse<AdminResponse.SeatInfo>> createSeat(
            @Valid @RequestBody AdminRequest.SeatCreate req) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createSeat(req)));
    }
}
