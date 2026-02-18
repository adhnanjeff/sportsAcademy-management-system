package com.badminton.academy.service;

import com.badminton.academy.model.Parent;
import com.badminton.academy.model.User;
import com.badminton.academy.repository.ParentRepository;
import com.badminton.academy.repository.StudentRepository;
import com.badminton.academy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final UserRepository userRepository;

    /**
     * Check if the currently authenticated user is the same as the user with the given ID
     */
    public boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * Check if the currently authenticated user is the parent of the student with the given ID
     */
    public boolean isParentOfStudent(Long studentId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        Optional<Parent> parentOpt = parentRepository.findById(currentUserId);
        if (parentOpt.isPresent()) {
            Parent parent = parentOpt.get();
            return parent.getChildren().stream()
                    .anyMatch(child -> child.getId().equals(studentId));
        }
        return false;
    }

    /**
     * Check if the currently authenticated user is the coach of the student with the given ID
     */
    public boolean isCoachOfStudent(Long studentId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        return studentRepository.findByCoachId(currentUserId).stream()
                .anyMatch(student -> student.getId().equals(studentId));
    }

    /**
     * Check if the currently authenticated user is the coach of the batch with the given ID
     */
    public boolean isCoachOfBatch(Long batchId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        return studentRepository.findByBatchId(batchId).stream()
                .flatMap(student -> student.getBatches().stream())
                .filter(batch -> batch.getId().equals(batchId))
                .anyMatch(batch -> batch.getCoach().getId().equals(currentUserId));
    }

    /**
     * Get the current authenticated user's ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getId();
            }
            String email = authentication.getName();
            if (email != null && !email.isBlank()) {
                return userRepository.findByEmail(email).map(User::getId).orElse(null);
            }
        }
        return null;
    }
}
