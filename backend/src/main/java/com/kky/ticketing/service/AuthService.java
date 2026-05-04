package com.kky.ticketing.service;

import com.kky.ticketing.config.JwtProvider;
import com.kky.ticketing.domain.entity.User;
import com.kky.ticketing.domain.repository.UserRepository;
import com.kky.ticketing.dto.request.LoginRequest;
import com.kky.ticketing.dto.request.SignupRequest;
import com.kky.ticketing.exception.BusinessException;
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
            throw new BusinessException(409, "EMAIL_ALREADY_EXISTS");
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
                .orElseThrow(() -> new BusinessException(401, "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(req.getPasswd(), user.getPasswd())) {
            throw new BusinessException(401, "INVALID_CREDENTIALS");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        redisTemplate.opsForValue()
                .set("refresh:" + user.getEmail(), refreshToken, Duration.ofDays(7));

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "expiresIn", 3600L
        );
    }
}
