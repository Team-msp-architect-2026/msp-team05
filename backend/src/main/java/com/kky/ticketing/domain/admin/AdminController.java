package com.kky.ticketing.domain.admin;

import com.kky.ticketing.dto.response.ApiResponse;
import com.kky.ticketing.domain.admin.dto.AdminRequest;
import com.kky.ticketing.domain.admin.dto.AdminResponse;
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
        return ResponseEntity.ok(ApiResponse.ok(adminService.createTeam(req)));
    }

    @PostMapping("/stadiums")
    public ResponseEntity<ApiResponse<AdminResponse.StadiumInfo>> createStadium(
            @Valid @RequestBody AdminRequest.StadiumCreate req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createStadium(req)));
    }

    @PostMapping("/games")
    public ResponseEntity<ApiResponse<AdminResponse.GameInfo>> createGame(
            @Valid @RequestBody AdminRequest.GameCreate req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createGame(req)));
    }

    @PostMapping("/zones")
    public ResponseEntity<ApiResponse<AdminResponse.ZoneInfo>> createZone(
            @Valid @RequestBody AdminRequest.ZoneCreate req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createZone(req)));
    }

    @PostMapping("/seats")
    public ResponseEntity<ApiResponse<AdminResponse.SeatInfo>> createSeat(
            @Valid @RequestBody AdminRequest.SeatCreate req) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.createSeat(req)));
    }
}
