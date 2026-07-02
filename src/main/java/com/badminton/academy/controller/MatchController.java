package com.badminton.academy.controller;

import com.badminton.academy.dto.request.CreateMatchRequest;
import com.badminton.academy.dto.response.HeadToHeadResponse;
import com.badminton.academy.dto.response.MatchResponse;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<MatchResponse>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<MatchResponse> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId) or @securityService.isParentOfStudent(#studentId)")
    public ResponseEntity<List<MatchResponse>> getMatchesByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(matchService.getMatchesByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<Map<String, Object>> getMatchStats(@PathVariable Long studentId) {
        return ResponseEntity.ok(matchService.getMatchStatsByStudent(studentId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.ok(MessageResponse.success("Match deleted successfully"));
    }

    @GetMapping("/head-to-head")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<HeadToHeadResponse> getHeadToHead(
            @RequestParam Long student1Id,
            @RequestParam Long student2Id) {
        return ResponseEntity.ok(matchService.getHeadToHead(student1Id, student2Id));
    }
}
