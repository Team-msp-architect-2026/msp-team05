package com.kky.ticketing.service;

import com.kky.ticketing.domain.entity.Payment;
import com.kky.ticketing.domain.entity.User;
import com.kky.ticketing.domain.repository.PaymentRepository;
import com.kky.ticketing.domain.repository.UserRepository;
import com.kky.ticketing.dto.request.PaymentConfirmRequest;
import com.kky.ticketing.dto.request.PaymentPrepareRequest;
import com.kky.ticketing.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public Map<String, Object> prepare(String userEmail, PaymentPrepareRequest req) {
        String lockData = redisTemplate.opsForValue().get("lock:" + req.getLockToken());
        if (lockData == null) {
            throw new BusinessException(400, "INVALID_LOCK_TOKEN");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(401, "USER_NOT_FOUND"));

        String orderId = UUID.randomUUID().toString();
        String pgToken = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .orderId(orderId)
                .pgToken(pgToken)
                .user(user)
                .paymentMethod(req.getPaymentMethod())
                .amount(0)
                .build();
        paymentRepository.save(payment);

        // orderId → lockToken 매핑 (10분 TTL)
        redisTemplate.opsForValue().set("order:" + orderId, req.getLockToken(), Duration.ofMinutes(10));

        return Map.of("orderId", orderId, "pgToken", pgToken);
    }

    @Transactional
    public Map<String, Object> confirm(PaymentConfirmRequest req) {
        Payment payment = paymentRepository.findByOrderId(req.getOrderId())
                .orElseThrow(() -> new BusinessException(400, "INVALID_ORDER_ID"));

        if (!payment.getPgToken().equals(req.getPgPaymentId())) {
            payment.fail();
            paymentRepository.save(payment);
            throw new BusinessException(400, "PAYMENT_FAILED");
        }

        payment.confirm();
        paymentRepository.save(payment);

        return Map.of("paymentId", payment.getId());
    }
}
