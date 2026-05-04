package com.kky.ticketing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kky.ticketing.domain.entity.Payment;
import com.kky.ticketing.domain.entity.Reservation;
import com.kky.ticketing.domain.entity.User;
import com.kky.ticketing.domain.repository.PaymentRepository;
import com.kky.ticketing.domain.repository.ReservationRepository;
import com.kky.ticketing.domain.repository.UserRepository;
import com.kky.ticketing.dto.request.ReservationRequest;
import com.kky.ticketing.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> create(String userEmail, ReservationRequest req) {
        String lockData = redisTemplate.opsForValue().get("lock:" + req.getLockToken());
        if (lockData == null) throw new BusinessException(400, "INVALID_LOCK_TOKEN");

        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new BusinessException(400, "INVALID_PAYMENT"));
        if (payment.getStatus() != Payment.PaymentStatus.PAID)
            throw new BusinessException(400, "PAYMENT_NOT_CONFIRMED");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(401, "USER_NOT_FOUND"));

        String reservationNumber = "KKY-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Reservation reservation = Reservation.builder()
                .reservationNumber(reservationNumber)
                .user(user)
                .payment(payment)
                .seatData(lockData)
                .ticketType(req.getTicketType())
                .build();
        reservationRepository.save(reservation);

        redisTemplate.delete("lock:" + req.getLockToken());

        return Map.of("reservationId", reservation.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMyReservations(String userEmail) {
        List<Map<String, Object>> list = reservationRepository.findByUserEmail(userEmail).stream()
                .map(r -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("reservationId", String.valueOf(r.getId()));
                    item.put("reservationNumber", r.getReservationNumber());
                    item.put("game", mockGame(r.getSeatData()));
                    item.put("totalAmount", r.getPayment().getAmount());
                    item.put("status", r.getStatus().name());
                    item.put("paidAt", r.getPayment().getCreatedAt() != null
                            ? r.getPayment().getCreatedAt().toString() : "");
                    return item;
                })
                .toList();
        return Map.of("content", list);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDetail(String userEmail, String reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(404, "RESERVATION_NOT_FOUND"));

        if (!r.getUser().getEmail().equals(userEmail))
            throw new BusinessException(403, "FORBIDDEN");

        List<String> seatIds = parseSeatIds(r.getSeatData());

        List<Map<String, Object>> tickets = new ArrayList<>();
        for (int i = 0; i < seatIds.size(); i++) {
            String seatId = seatIds.get(i);
            Map<String, Object> ticket = new LinkedHashMap<>();
            ticket.put("ticketId", r.getId() + "-" + i);
            ticket.put("seatInfo", seatId);
            ticket.put("qrCode", generateQrCode(r.getReservationNumber() + "-" + seatId));
            tickets.add(ticket);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reservationId", String.valueOf(r.getId()));
        result.put("reservationNumber", r.getReservationNumber());
        result.put("game", mockGame(r.getSeatData()));
        result.put("tickets", tickets);
        result.put("totalAmount", r.getPayment().getAmount());
        result.put("status", r.getStatus().name());
        result.put("paidAt", r.getPayment().getCreatedAt() != null
                ? r.getPayment().getCreatedAt().toString() : "");
        return result;
    }

    @Transactional
    public void cancel(String userEmail, String reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(404, "RESERVATION_NOT_FOUND"));

        if (!r.getUser().getEmail().equals(userEmail))
            throw new BusinessException(403, "FORBIDDEN");

        if (r.getStatus() == Reservation.ReservationStatus.CANCELLED)
            throw new BusinessException(400, "ALREADY_CANCELLED");

        r.cancel();
        reservationRepository.save(r);
    }

    // QR코드 → data:image/png;base64,... 형태
    private String generateQrCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    // seatData JSON에서 seatIds 파싱
    private List<String> parseSeatIds(String seatData) {
        try {
            JsonNode node = objectMapper.readTree(seatData);
            List<String> ids = new ArrayList<>();
            node.get("seatIds").forEach(n -> ids.add(n.asText()));
            return ids;
        } catch (Exception e) {
            return List.of();
        }
    }

    // Game 엔티티 구현 전까지 mock 데이터 반환
    private Map<String, Object> mockGame(String seatData) {
        Map<String, Object> game = new LinkedHashMap<>();
        game.put("homeTeam", "한화");
        game.put("awayTeam", "KIA");
        game.put("startTime", "2026-05-10T18:00:00");
        return game;
    }
}
