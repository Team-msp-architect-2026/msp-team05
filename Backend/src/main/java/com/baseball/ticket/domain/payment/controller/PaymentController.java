package com.baseball.ticket.domain.payment.controller;

import com.baseball.ticket.domain.auth.entity.User;
import com.baseball.ticket.domain.auth.repository.UserRepository;
import com.baseball.ticket.domain.payment.entity.Payment;
import com.baseball.ticket.domain.payment.repository.PaymentRepository;
import com.baseball.ticket.domain.seat.entity.SeatLock;
import com.baseball.ticket.domain.seat.repository.SeatLockRepository;
import com.baseball.ticket.domain.seat.repository.SeatZoneRepository;
import com.baseball.ticket.domain.seat.repository.SeatRepository;
import com.baseball.ticket.global.exception.BusinessException;
import com.baseball.ticket.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SeatLockRepository seatLockRepository;
    private final SeatRepository seatRepository;
    private final SeatZoneRepository seatZoneRepository;

    @Transactional
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<Map<String, Object>>> prepare(
            @RequestBody Map<String, String> req,
            @AuthenticationPrincipal String email) {

        String lockToken = req.get("lockToken");
        String paymentMethod = req.get("paymentMethod");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", 404));

        //SeatLock seatLock = seatLockRepository.findByLockToken(lockToken)
        //        .orElseThrow(() -> new BusinessException("LOCK_NOT_FOUND", 404));

        // 여러 좌석 모두 가져오기
        List<SeatLock> seatLocks = seatLockRepository.findAllByLockToken(lockToken);
        if (seatLocks.isEmpty()) {
            throw new BusinessException("LOCK_NOT_FOUND", 404);
        }

        // 각 좌석의 구역 가격 합산
        int amount = 0;
        for (SeatLock seatLock : seatLocks) {
            String zoneId = seatLock.getSeat().getZone().getId();
            int price = seatZoneRepository.findById(zoneId)
                    .map(zone -> zone.getPrice())
                    .orElse(15000);
            amount += price;
        }

        String orderId = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .orderId(orderId)
                .pgPaymentId(orderId)
                .user(user)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .build();
        paymentRepository.save(payment);

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "orderId", orderId,
                "amount", amount,
                "status", "READY"
        )));
    }

    @Transactional
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirm(
            @RequestBody Map<String, String> req) {

        String orderId = req.get("orderId");
        String pgPaymentId = req.get("pgPaymentId");

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", 404));
        payment.confirm();
        paymentRepository.save(payment);

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "pgPaymentId", pgPaymentId,
                "status", "DONE"
        )));
    }
}