package com.kky.ticketing.controller;

import com.kky.ticketing.dto.request.LoginRequest;
import com.kky.ticketing.dto.request.SignupRequest;
import com.kky.ticketing.dto.response.ApiResponse;
import com.kky.ticketing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest req) {
        authService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest req) {
        Map<String, Object> tokens = authService.login(req);
        return ResponseEntity.ok(ApiResponse.ok(tokens));
    }
}
