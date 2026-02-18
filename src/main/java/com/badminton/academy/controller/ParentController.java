package com.badminton.academy.controller;

import com.badminton.academy.dto.request.UpdateParentRequest;
import com.badminton.academy.dto.response.MessageResponse;
import com.badminton.academy.dto.response.ParentResponse;
import com.badminton.academy.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ParentResponse>> getAllParents() {
        return ResponseEntity.ok(parentService.getAllParents());
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ParentResponse>> getActiveParents() {
        return ResponseEntity.ok(parentService.getActiveParents());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<ParentResponse> getParentById(@PathVariable Long id) {
        return ResponseEntity.ok(parentService.getParentById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParentResponse> getParentByEmail(@PathVariable String email) {
        return ResponseEntity.ok(parentService.getParentByEmail(email));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH') or @securityService.isCurrentUser(#studentId)")
    public ResponseEntity<ParentResponse> getParentByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(parentService.getParentByStudent(studentId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<ParentResponse> updateParent(@PathVariable Long id, @Valid @RequestBody UpdateParentRequest request) {
        return ResponseEntity.ok(parentService.updateParent(id, request));
    }

    @PostMapping("/{parentId}/children/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParentResponse> addChild(@PathVariable Long parentId, @PathVariable Long studentId) {
        return ResponseEntity.ok(parentService.addChild(parentId, studentId));
    }

    @DeleteMapping("/{parentId}/children/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParentResponse> removeChild(@PathVariable Long parentId, @PathVariable Long studentId) {
        return ResponseEntity.ok(parentService.removeChild(parentId, studentId));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deactivateParent(@PathVariable Long id) {
        parentService.deactivateParent(id);
        return ResponseEntity.ok(MessageResponse.success("Parent deactivated successfully"));
    }

    @GetMapping("/{parentId}/children/count")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#parentId)")
    public ResponseEntity<Long> countChildren(@PathVariable Long parentId) {
        return ResponseEntity.ok(parentService.countChildren(parentId));
    }
}
