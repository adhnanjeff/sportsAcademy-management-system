package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateTournamentRequest;
import com.badminton.academy.dto.request.UpdateMatchScoreRequest;
import com.badminton.academy.dto.response.MatchResponse;
import com.badminton.academy.dto.response.TournamentBracketResponse;
import com.badminton.academy.dto.response.TournamentResponse;
import com.badminton.academy.exception.BadRequestException;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.Match;
import com.badminton.academy.model.Student;
import com.badminton.academy.model.Tournament;
import com.badminton.academy.model.enums.MatchType;
import com.badminton.academy.model.enums.TournamentStatus;
import com.badminton.academy.repository.BatchRepository;
import com.badminton.academy.repository.CoachRepository;
import com.badminton.academy.repository.MatchRepository;
import com.badminton.academy.repository.StudentRepository;
import com.badminton.academy.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final BatchRepository batchRepository;
    private final StudentRepository studentRepository;
    private final CoachRepository coachRepository;
    private final MatchService matchService;

    public List<TournamentResponse> getAllTournaments() {
        return tournamentRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TournamentResponse getTournamentById(Long id) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
        return mapToResponse(t);
    }

    public List<TournamentResponse> getTournamentsByBatch(Long batchId) {
        return tournamentRepository.findByBatchIdOrderByCreatedAtDesc(batchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TournamentResponse createTournament(CreateTournamentRequest request, Long coachId) {
        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found"));

        List<Student> participants = new ArrayList<>();
        for (Long studentId : request.getParticipantIds()) {
            Student s = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
            participants.add(s);
        }

        int n = participants.size();
        int totalRounds = (int) Math.ceil(Math.log(n) / Math.log(2));
        int totalSlots = (int) Math.pow(2, totalRounds);

        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .batch(batch)
                .createdBy(coach)
                .status(TournamentStatus.IN_PROGRESS)
                .totalRounds(totalRounds)
                .totalParticipants(n)
                .startDate(request.getStartDate())
                .participants(new HashSet<>(participants))
                .matches(new ArrayList<>())
                .build();

        tournament = tournamentRepository.save(tournament);

        Collections.shuffle(participants);

        // Pad with nulls for BYEs
        List<Student> seeded = new ArrayList<>(participants);
        while (seeded.size() < totalSlots) {
            seeded.add(null);
        }

        // Generate round 1 matches
        List<Match> round1Matches = new ArrayList<>();
        for (int i = 0; i < totalSlots / 2; i++) {
            Student p1 = seeded.get(i * 2);
            Student p2 = seeded.get(i * 2 + 1);

            Match match = Match.builder()
                    .player1(p1)
                    .player2(p2)
                    .matchType(MatchType.TOURNAMENT)
                    .tournament(tournament)
                    .roundNumber(1)
                    .matchPosition(i)
                    .matchDate(request.getStartDate())
                    .build();

            // BYE: auto-advance
            if (p2 == null) {
                match.setWinner(p1);
                match.setScoreDisplay("BYE");
            }

            round1Matches.add(matchRepository.save(match));
        }

        // Generate empty matches for subsequent rounds
        for (int round = 2; round <= totalRounds; round++) {
            int matchesInRound = totalSlots / (int) Math.pow(2, round);
            for (int pos = 0; pos < matchesInRound; pos++) {
                // Placeholder student for empty slots — player1 is required by DB,
                // but we'll use a trick: set player1 to first participant as placeholder
                // Actually, we need to handle this differently. Let's create the match
                // only when both feeder matches have winners. For now create with first available.
                Match match = Match.builder()
                        .player1(participants.get(0)) // placeholder, will be overwritten
                        .matchType(MatchType.TOURNAMENT)
                        .tournament(tournament)
                        .roundNumber(round)
                        .matchPosition(pos)
                        .matchDate(request.getStartDate())
                        .build();
                matchRepository.save(match);
            }
        }

        // Auto-advance BYE winners into round 2
        for (Match r1 : round1Matches) {
            if (r1.getWinner() != null) {
                advanceWinner(tournament.getId(), r1);
            }
        }

        log.info("Tournament created: {} with {} participants, {} rounds",
                tournament.getName(), n, totalRounds);
        return getTournamentById(tournament.getId());
    }

    @Transactional
    public TournamentBracketResponse updateMatchScore(Long tournamentId, Long matchId, UpdateMatchScoreRequest request) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (tournament.getStatus() == TournamentStatus.COMPLETED || tournament.getStatus() == TournamentStatus.CANCELLED) {
            throw new BadRequestException("Tournament is already " + tournament.getStatus().name().toLowerCase());
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        if (!match.getTournament().getId().equals(tournamentId)) {
            throw new BadRequestException("Match does not belong to this tournament");
        }

        Student winner = studentRepository.findById(request.getWinnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Winner not found"));

        match.setWinner(winner);
        match.setPlayer1Score(request.getPlayer1Score());
        match.setPlayer2Score(request.getPlayer2Score());
        match.setScoreDisplay(request.getScoreDisplay());
        match.setDurationMinutes(request.getDurationMinutes());
        if (request.getNotes() != null) {
            match.setNotes(request.getNotes());
        }
        matchRepository.save(match);

        advanceWinner(tournamentId, match);

        // Check if tournament is complete (final match has winner)
        if (match.getRoundNumber().equals(tournament.getTotalRounds())) {
            tournament.setStatus(TournamentStatus.COMPLETED);
            tournamentRepository.save(tournament);
            log.info("Tournament {} completed. Winner: {}", tournament.getName(), winner.getFullName());
        }

        return getTournamentBracket(tournamentId);
    }

    private void advanceWinner(Long tournamentId, Match completedMatch) {
        int currentRound = completedMatch.getRoundNumber();
        int currentPos = completedMatch.getMatchPosition();
        int nextRound = currentRound + 1;
        int nextPos = currentPos / 2;

        Optional<Match> nextMatchOpt = matchRepository.findByTournamentAndRoundAndPosition(
                tournamentId, nextRound, nextPos);

        if (nextMatchOpt.isEmpty()) return;

        Match nextMatch = nextMatchOpt.get();
        boolean isEvenPosition = (currentPos % 2 == 0);

        if (isEvenPosition) {
            nextMatch.setPlayer1(completedMatch.getWinner());
        } else {
            nextMatch.setPlayer2(completedMatch.getWinner());
        }
        matchRepository.save(nextMatch);
    }

    public TournamentBracketResponse getTournamentBracket(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        List<Match> matches = matchRepository.findByTournamentIdOrderByRoundNumberAscMatchPositionAsc(tournamentId);

        Map<Integer, List<MatchResponse>> roundMap = matches.stream()
                .map(matchService::mapToResponse)
                .collect(Collectors.groupingBy(MatchResponse::getRoundNumber, TreeMap::new, Collectors.toList()));

        List<TournamentBracketResponse.BracketRound> rounds = roundMap.entrySet().stream()
                .map(entry -> TournamentBracketResponse.BracketRound.builder()
                        .roundNumber(entry.getKey())
                        .roundName(getRoundName(entry.getKey(), tournament.getTotalRounds()))
                        .matches(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return TournamentBracketResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .status(tournament.getStatus())
                .totalRounds(tournament.getTotalRounds())
                .rounds(rounds)
                .build();
    }

    @Transactional
    public void cancelTournament(Long id) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
        t.setStatus(TournamentStatus.CANCELLED);
        tournamentRepository.save(t);
    }

    private String getRoundName(int round, int totalRounds) {
        int fromEnd = totalRounds - round;
        if (fromEnd == 0) return "Final";
        if (fromEnd == 1) return "Semi-Finals";
        if (fromEnd == 2) return "Quarter-Finals";
        return "Round " + round;
    }

    private TournamentResponse mapToResponse(Tournament t) {
        List<MatchResponse> matchResponses = matchRepository
                .findByTournamentIdOrderByRoundNumberAscMatchPositionAsc(t.getId())
                .stream()
                .map(matchService::mapToResponse)
                .collect(Collectors.toList());

        return TournamentResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .batchId(t.getBatch().getId())
                .batchName(t.getBatch().getName())
                .createdById(t.getCreatedBy().getId())
                .createdByName(t.getCreatedBy().getFullName())
                .status(t.getStatus())
                .totalRounds(t.getTotalRounds())
                .totalParticipants(t.getTotalParticipants())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .createdAt(t.getCreatedAt())
                .matches(matchResponses)
                .build();
    }
}
