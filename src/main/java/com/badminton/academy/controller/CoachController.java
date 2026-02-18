package com.badminton.academy.controller;

import com.badminton.academy.dto.request.RegisterRequest;
import com.badminton.academy.dto.request.UpdateCoachRequest;
import com.badminton.academy.dto.response.CoachResponse;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.model.enums.Role;
import com.badminton.academy.service.AuthService;
import com.badminton.academy.service.CoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CoachResponse>> getAllCoaches() {
        return ResponseEntity.ok(coachService.getAllCoaches());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoachResponse> createCoach(@Valid @RequestBody RegisterRequest request) {
        request.setRole(Role.COACH);
        Long coachId = authService.register(request).getUser().getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(coachService.getCoachById(coachId));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<List<CoachResponse>> getActiveCoaches() {
        return ResponseEntity.ok(coachService.getActiveCoaches());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<CoachResponse> getCoachById(@PathVariable Long id) {
        return ResponseEntity.ok(coachService.getCoachById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoachResponse> getCoachByEmail(@PathVariable String email) {
        return ResponseEntity.ok(coachService.getCoachByEmail(email));
    }

    @GetMapping("/specialization")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public ResponseEntity<List<CoachResponse>> getCoachesBySpecialization(@RequestParam String spec) {
        return ResponseEntity.ok(coachService.getCoachesBySpecialization(spec));
    }

    @GetMapping("/experience")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CoachResponse>> getCoachesByMinimumExperience(@RequestParam Integer years) {
        return ResponseEntity.ok(coachService.getCoachesByMinimumExperience(years));
    }

    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or hasRole('STUDENT')")
    public ResponseEntity<CoachResponse> getCoachByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(coachService.getCoachByBatch(batchId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<CoachResponse> updateCoach(@PathVariable Long id, @Valid @RequestBody UpdateCoachRequest request) {
        return ResponseEntity.ok(coachService.updateCoach(id, request));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deactivateCoach(@PathVariable Long id) {
        coachService.deactivateCoach(id);
        return ResponseEntity.ok(MessageResponse.success("Coach deactivated successfully"));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> activateCoach(@PathVariable Long id) {
        coachService.activateCoach(id);
        return ResponseEntity.ok(MessageResponse.success("Coach activated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCoach(@PathVariable Long id) {
        coachService.deleteCoach(id);
        return ResponseEntity.ok(MessageResponse.success("Coach deleted successfully"));
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countActiveCoaches() {
        return ResponseEntity.ok(coachService.countActiveCoaches());
    }
}
