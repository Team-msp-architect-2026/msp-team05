package com.baseball.ticket.domain.game.scheduler;

import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameScheduler {

    private final GameRepository gameRepository;

    // 1분마다 경기 상태 확인
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void updateGameStatus() {
        List<Game> games = gameRepository
                .findByStatusAndStartTimeBefore(
                        Game.GameStatus.ON_SALE,
                        LocalDateTime.now()
                );

        for (Game game : games) {
            game.updateStatus("FINISHED");
            log.info("경기 상태 FINISHED 변경: gameId={}, startTime={}",
                    game.getId(), game.getStartTime());
        }
    }
}