package com.badminton.academy.controller;

import com.badminton.academy.dto.request.CreateTournamentRequest;
import com.badminton.academy.dto.request.UpdateMatchScoreRequest;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.dto.response.TournamentBracketResponse;
import com.badminton.academy.dto.response.TournamentResponse;
import com.badminton.academy.model.User;
import com.badminton.academy.service.AuthService;
import com.badminton.academy.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<TournamentResponse> getTournamentById(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getTournamentById(id));
    }

    @GetMapping("/{id}/bracket")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<TournamentBracketResponse> getTournamentBracket(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getTournamentBracket(id));
    }

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<TournamentResponse>> getTournamentsByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(tournamentService.getTournamentsByBatch(batchId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<TournamentResponse> createTournament(@Valid @RequestBody CreateTournamentRequest request) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tournamentService.createTournament(request, currentUser.getId()));
    }

    @PutMapping("/{id}/matches/{matchId}/score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<TournamentBracketResponse> updateMatchScore(
            @PathVariable Long id,
            @PathVariable Long matchId,
            @Valid @RequestBody UpdateMatchScoreRequest request) {
        return ResponseEntity.ok(tournamentService.updateMatchScore(id, matchId, request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> cancelTournament(@PathVariable Long id) {
        tournamentService.cancelTournament(id);
        return ResponseEntity.ok(MessageResponse.success("Tournament cancelled"));
    }
}
