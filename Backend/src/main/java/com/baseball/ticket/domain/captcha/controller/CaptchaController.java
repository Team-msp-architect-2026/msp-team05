package com.baseball.ticket.domain.captcha.controller;

import com.baseball.ticket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "Captcha", description = "CAPTCHA 생성 API")
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int CAPTCHA_TTL_MINUTES = 5;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 6;

    @Operation(summary = "CAPTCHA 이미지 생성")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> generateCaptcha() throws Exception {

        // 1. 랜덤 문자열 생성
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        String answer = sb.toString();

        // 2. 이미지 생성 (왜곡 효과 포함)
        int width = 180, height = 60;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 배경
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 노이즈 선
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 8; i++) {
            g.drawLine(
                    random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height)
            );
        }

        // 텍스트 (문자마다 색상/각도 다르게)
        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < answer.length(); i++) {
            g.setColor(new Color(
                    random.nextInt(100),
                    random.nextInt(100),
                    random.nextInt(150)
            ));
            g.drawString(
                    String.valueOf(answer.charAt(i)),
                    20 + i * 26,
                    40 + random.nextInt(10) - 5
            );
        }

        // 노이즈 점
        for (int i = 0; i < 50; i++) {
            g.setColor(Color.GRAY);
            g.fillOval(random.nextInt(width), random.nextInt(height), 2, 2);
        }

        g.dispose();

        // 3. 이미지 → Base64 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        String imageBase64 = Base64.getEncoder().encodeToString(baos.toByteArray());

        // 4. Redis에 저장 (TTL 5분)
        String captchaId = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "captcha:" + captchaId,
                answer,
                CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES
        );

        // 5. 응답
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "captchaId", captchaId,
                "imageBase64", "data:image/png;base64," + imageBase64
        )));
    }
}