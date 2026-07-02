package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateMatchRequest;
import com.badminton.academy.dto.response.HeadToHeadResponse;
import com.badminton.academy.dto.response.MatchResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Match;
import com.badminton.academy.model.Student;
import com.badminton.academy.repository.MatchRepository;
import com.badminton.academy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final StudentRepository studentRepository;

    public List<MatchResponse> getAllMatches() {
        return matchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MatchResponse getMatchById(Long id) {
        return mapToResponse(matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id)));
    }

    public List<MatchResponse> getMatchesByStudent(Long studentId) {
        return matchRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getMatchStatsByStudent(Long studentId) {
        List<Match> matches = matchRepository.findByStudentId(studentId);
        int wins = 0, losses = 0, draws = 0;
        for (Match m : matches) {
            if (m.getWinner() == null) {
                draws++;
            } else if (m.getWinner().getId().equals(studentId)) {
                wins++;
            } else {
                losses++;
            }
        }
        int total = matches.size();
        int winRate = total > 0 ? Math.round((wins * 100f) / total) : 0;
        return Map.of(
            "totalMatches", total,
            "wins", wins,
            "losses", losses,
            "draws", draws,
            "winRate", winRate
        );
    }

    @Transactional
    public MatchResponse createMatch(CreateMatchRequest request) {
        Student player1 = studentRepository.findById(request.getPlayer1Id())
                .orElseThrow(() -> new ResourceNotFoundException("Player 1 not found"));

        Student player2 = null;
        if (request.getPlayer2Id() != null) {
            player2 = studentRepository.findById(request.getPlayer2Id())
                    .orElseThrow(() -> new ResourceNotFoundException("Player 2 not found"));
        }

        Student winner = null;
        if (request.getWinnerId() != null) {
            winner = studentRepository.findById(request.getWinnerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Winner not found"));
        }

        Student partner1 = null;
        if (request.getPartner1Id() != null) {
            partner1 = studentRepository.findById(request.getPartner1Id()).orElse(null);
        }
        Student partner2 = null;
        if (request.getPartner2Id() != null) {
            partner2 = studentRepository.findById(request.getPartner2Id()).orElse(null);
        }

        Match match = Match.builder()
                .player1(player1)
                .player2(player2)
                .winner(winner)
                .matchType(request.getMatchType())
                .player1Score(request.getPlayer1Score())
                .player2Score(request.getPlayer2Score())
                .scoreDisplay(request.getScoreDisplay())
                .partner1(partner1)
                .partner2(partner2)
                .matchDate(request.getMatchDate())
                .durationMinutes(request.getDurationMinutes())
                .notes(request.getNotes())
                .location(request.getLocation())
                .eventName(request.getEventName())
                .build();

        Match saved = matchRepository.save(match);
        log.info("Match created: {} vs {}", player1.getFullName(), player2 != null ? player2.getFullName() : "BYE");
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteMatch(Long id) {
        if (!matchRepository.existsById(id)) {
            throw new ResourceNotFoundException("Match not found with id: " + id);
        }
        matchRepository.deleteById(id);
    }

    public HeadToHeadResponse getHeadToHead(Long student1Id, Long student2Id) {
        Student s1 = studentRepository.findById(student1Id)
                .orElseThrow(() -> new ResourceNotFoundException("Student 1 not found"));
        Student s2 = studentRepository.findById(student2Id)
                .orElseThrow(() -> new ResourceNotFoundException("Student 2 not found"));

        List<Match> matches = matchRepository.findHeadToHead(student1Id, student2Id);
        int s1Wins = 0, s2Wins = 0, draws = 0;
        for (Match m : matches) {
            if (m.getWinner() == null) draws++;
            else if (m.getWinner().getId().equals(student1Id)) s1Wins++;
            else if (m.getWinner().getId().equals(student2Id)) s2Wins++;
        }

        List<MatchResponse> recent = matches.stream()
                .limit(5)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return HeadToHeadResponse.builder()
                .student1Id(student1Id)
                .student1Name(s1.getFullName())
                .student2Id(student2Id)
                .student2Name(s2.getFullName())
                .student1Wins(s1Wins)
                .student2Wins(s2Wins)
                .draws(draws)
                .totalMatches(matches.size())
                .recentMatches(recent)
                .build();
    }

    public MatchResponse mapToResponse(Match m) {
        return MatchResponse.builder()
                .id(m.getId())
                .player1Id(m.getPlayer1().getId())
                .player1Name(m.getPlayer1().getFullName())
                .player2Id(m.getPlayer2() != null ? m.getPlayer2().getId() : null)
                .player2Name(m.getPlayer2() != null ? m.getPlayer2().getFullName() : "BYE")
                .winnerId(m.getWinner() != null ? m.getWinner().getId() : null)
                .winnerName(m.getWinner() != null ? m.getWinner().getFullName() : null)
                .matchType(m.getMatchType())
                .player1Score(m.getPlayer1Score())
                .player2Score(m.getPlayer2Score())
                .scoreDisplay(m.getScoreDisplay())
                .tournamentId(m.getTournament() != null ? m.getTournament().getId() : null)
                .roundNumber(m.getRoundNumber())
                .matchPosition(m.getMatchPosition())
                .partner1Id(m.getPartner1() != null ? m.getPartner1().getId() : null)
                .partner1Name(m.getPartner1() != null ? m.getPartner1().getFullName() : null)
                .partner2Id(m.getPartner2() != null ? m.getPartner2().getId() : null)
                .partner2Name(m.getPartner2() != null ? m.getPartner2().getFullName() : null)
                .matchDate(m.getMatchDate())
                .durationMinutes(m.getDurationMinutes())
                .notes(m.getNotes())
                .location(m.getLocation())
                .eventName(m.getEventName())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
