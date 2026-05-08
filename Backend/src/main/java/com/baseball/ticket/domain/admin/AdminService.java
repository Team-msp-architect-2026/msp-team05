package com.baseball.ticket.domain.admin;

import java.util.List;
import java.util.stream.Collectors;
import com.baseball.ticket.domain.admin.dto.AdminRequest;
import com.baseball.ticket.domain.admin.dto.AdminResponse;
import com.baseball.ticket.domain.game.entity.Game;
import com.baseball.ticket.domain.game.entity.Stadium;
import com.baseball.ticket.domain.game.entity.Team;
import com.baseball.ticket.domain.game.repository.GameRepository;
import com.baseball.ticket.domain.game.repository.StadiumRepository;
import com.baseball.ticket.domain.game.repository.TeamRepository;
import com.baseball.ticket.domain.seat.entity.Seat;
import com.baseball.ticket.domain.seat.entity.SeatZone;
import com.baseball.ticket.domain.seat.repository.SeatRepository;
import com.baseball.ticket.domain.seat.repository.SeatZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;
    private final SeatZoneRepository seatZoneRepository;
    private final SeatRepository seatRepository;

    public AdminResponse.TeamInfo createTeam(AdminRequest.TeamCreate req) {
        Team team = Team.builder()
                .name(req.getName())
                .logoUrl(req.getLogoUrl())
                .build();
        return AdminResponse.TeamInfo.from(teamRepository.save(team));
    }

    public AdminResponse.StadiumInfo createStadium(AdminRequest.StadiumCreate req) {
        Stadium stadium = Stadium.builder()
                .name(req.getName())
                .address(req.getAddress())
                .build();
        return AdminResponse.StadiumInfo.from(stadiumRepository.save(stadium));
    }

    public AdminResponse.GameInfo createGame(AdminRequest.GameCreate req) {
        Team homeTeam = teamRepository.findById(req.getHomeTeamId())
                .orElseThrow(() -> new IllegalArgumentException("홈팀을 찾을 수 없습니다."));
        Team awayTeam = teamRepository.findById(req.getAwayTeamId())
                .orElseThrow(() -> new IllegalArgumentException("원정팀을 찾을 수 없습니다."));
        Stadium stadium = stadiumRepository.findById(req.getStadiumId())
                .orElseThrow(() -> new IllegalArgumentException("구장을 찾을 수 없습니다."));

        Game game = Game.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .startTime(req.getStartTime())
                .ticketOpenTime(req.getTicketOpenTime())
                .status(Game.GameStatus.SCHEDULED)
                .build();
        return AdminResponse.GameInfo.from(gameRepository.save(game));
    }

    public AdminResponse.ZoneInfo createZone(AdminRequest.ZoneCreate req) {
        Stadium stadium = stadiumRepository.findById(req.getStadiumId())
                .orElseThrow(() -> new IllegalArgumentException("구장을 찾을 수 없습니다."));

        SeatZone zone = SeatZone.builder()
                .stadium(stadium)
                .zoneName(req.getZoneName())
                .price(req.getPrice())
                .totalSeats(req.getTotalSeats())
                .build();
        return AdminResponse.ZoneInfo.from(seatZoneRepository.save(zone));
    }

    public AdminResponse.SeatInfo createSeat(AdminRequest.SeatCreate req) {
        SeatZone zone = seatZoneRepository.findById(req.getZoneId())
                .orElseThrow(() -> new IllegalArgumentException("구역을 찾을 수 없습니다."));

        Seat seat = Seat.builder()
                .zone(zone)
                .rowNum(req.getRow())
                .number(req.getNumber())
                .status("AVAILABLE")
                .build();
        return AdminResponse.SeatInfo.from(seatRepository.save(seat));
    }

    public List<AdminResponse.TeamInfo> getTeams() {
        return teamRepository.findAll().stream()
                .map(AdminResponse.TeamInfo::from)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<AdminResponse.StadiumInfo> getStadiums() {
        return stadiumRepository.findAll().stream()
                .map(AdminResponse.StadiumInfo::from)
                .collect(java.util.stream.Collectors.toList());
    }
}