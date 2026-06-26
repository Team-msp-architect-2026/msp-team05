package com.baseball.ticket.domain.reservation.service;

import com.baseball.ticket.domain.auth.entity.User;
import com.baseball.ticket.domain.auth.repository.UserRepository;
import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.repository.GameRepository;
import com.baseball.ticket.domain.reservation.dto.ReservationRequest;
import com.baseball.ticket.domain.reservation.dto.ReservationResponse;
import com.baseball.ticket.domain.reservation.entity.Reservation;
import com.baseball.ticket.domain.reservation.repository.ReservationRepository;
import com.baseball.ticket.domain.seat.entity.Seat;
import com.baseball.ticket.domain.seat.entity.SeatLock;
import com.baseball.ticket.domain.seat.repository.SeatLockRepository;
import com.baseball.ticket.domain.seat.repository.SeatRepository;
import com.baseball.ticket.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatLockRepository seatLockRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final SeatRepository seatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;


    @Transactional
    public ReservationResponse createReservation(ReservationRequest req, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", 404));

        List<SeatLock> seatLocks = seatLockRepository.findAllByLockToken(req.getLockToken());
        if (seatLocks.isEmpty()) {
            throw new BusinessException("LOCK_NOT_FOUND", 404);
        }

        Game game = gameRepository.findById(req.getGameId())
                .orElseThrow(() -> new BusinessException("GAME_NOT_FOUND", 404));

        Reservation lastReservation = null;
        Seat lastSeat = null;

        for (SeatLock seatLock : seatLocks) {
            Seat seat = seatLock.getSeat();
            seat.updateStatus("RESERVED");

            String redisKey = "seat_lock:" + req.getGameId() + ":" + seat.getId();
            redisTemplate.delete(redisKey);

            messagingTemplate.convertAndSend(
                    "/topic/seats/" + req.getGameId(),
                    Map.of("seatId", seat.getId(), "status", "RESERVED")
            );

            Reservation reservation = Reservation.builder()
                    .userId(user.getId())
                    .gameId(req.getGameId())
                    .seatId(seat.getId())
                    .lockToken(req.getLockToken())
                    .build();

            lastReservation = reservationRepository.save(reservation);
            lastSeat = seat;
        }

        return ReservationResponse.from(lastReservation, game, lastSeat);
    }

    /*
    @Transactional
    public ReservationResponse createReservation(ReservationRequest req, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", 404));

        SeatLock seatLock = seatLockRepository.findByLockToken(req.getLockToken())
                .orElseThrow(() -> new BusinessException("LOCK_NOT_FOUND", 404));

        Game game = gameRepository.findById(req.getGameId())
                .orElseThrow(() -> new BusinessException("GAME_NOT_FOUND", 404));

        // 좌석 상태 RESERVED로 변경 추가
        Seat seat = seatLock.getSeat();
        seat.updateStatus("RESERVED");

        messagingTemplate.convertAndSend(
                "/topic/seats/" + req.getGameId(),
                Map.of("seatId", seat.getId(), "status", "RESERVED")
        );

        Reservation reservation = Reservation.builder()
                .userId(user.getId())
                .gameId(req.getGameId())
                .seatId(seatLock.getSeat().getId())
                .lockToken(req.getLockToken())
                .build();

        return ReservationResponse.from(
                reservationRepository.save(reservation), game, seat);
    }

     */

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", 404));

        return reservationRepository.findByUserId(user.getId())
                .stream()
                .map(r -> {
                    Game game = gameRepository.findById(r.getGameId())
                            .orElseThrow(() -> new BusinessException("GAME_NOT_FOUND", 404));
                    Seat seat = seatRepository.findById(r.getSeatId())
                            .orElseThrow(() -> new BusinessException("SEAT_NOT_FOUND", 404));
                    return ReservationResponse.from(r, game, seat);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelReservation(String reservationId, String email) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND", 404));

        // 좌석 상태 AVAILABLE로 복구
        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new BusinessException("SEAT_NOT_FOUND", 404));
        seat.updateStatus("AVAILABLE");

        // WebSocket 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/seats/" + reservation.getGameId(),
                Map.of("seatId", seat.getId(), "status", "AVAILABLE")
        );

        reservationRepository.delete(reservation);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(String reservationId, String email) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND", 404));

        Game game = gameRepository.findById(reservation.getGameId())
                .orElseThrow(() -> new BusinessException("GAME_NOT_FOUND", 404));

        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new BusinessException("SEAT_NOT_FOUND", 404));

        return ReservationResponse.from(reservation, game, seat);
    }
}