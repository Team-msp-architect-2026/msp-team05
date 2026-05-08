package com.baseball.ticket.domain.auth.service;

import com.baseball.ticket.domain.auth.repository.UserRepository;
import com.baseball.ticket.global.jwt.JwtProvider;
import com.baseball.ticket.domain.auth.entity.User;
import com.baseball.ticket.domain.auth.dto.request.LoginRequest;
import com.baseball.ticket.domain.auth.dto.request.SignupRequest;
import com.baseball.ticket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("EMAIL_ALREADY_EXISTS", 409);
        }
        User user = User.builder()
                .email(req.getEmail())
                .username(req.getUsername())
                .passwd(passwordEncoder.encode(req.getPasswd()))
                .phonenum(req.getPhonenum())
                .address(req.getAddress())
                .build();
        userRepository.save(user);
    }

    @Transactional
    public Map<String, Object> login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", 401));

        if (!passwordEncoder.matches(req.getPasswd(), user.getPasswd())) {
            throw new BusinessException("INVALID_CREDENTIALS", 401);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        redisTemplate.opsForValue()
                .set("refresh:" + user.getEmail(), refreshToken, Duration.ofDays(7));

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "expiresIn", 3600L
        );
    }

    public Map<String, Object> refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException("INVALID_TOKEN", 401);
        }
        String email = jwtProvider.getEmail(refreshToken);
        String saved = redisTemplate.opsForValue().get("refresh:" + email);
        if (!refreshToken.equals(saved)) {
            throw new BusinessException("INVALID_TOKEN", 401);
        }
        //String newAccessToken = jwtProvider.generateAccessToken(email, "USER");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", 404));
        String newAccessToken = jwtProvider.generateAccessToken(email, user.getRole());
        return Map.of("accessToken", newAccessToken, "expiresIn", 3600L);
    }

    public void logout(String accessToken) {
        if (!jwtProvider.validateToken(accessToken)) return;
        String email = jwtProvider.getEmail(accessToken);
        redisTemplate.delete("refresh:" + email);
    }
}
