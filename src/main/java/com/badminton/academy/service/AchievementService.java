package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateAchievementRequest;
import com.badminton.academy.dto.response.AchievementResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Achievement;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.Student;
import com.badminton.academy.model.enums.AchievementType;
import com.badminton.academy.repository.AchievementRepository;
import com.badminton.academy.repository.CoachRepository;
import com.badminton.academy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final StudentRepository studentRepository;
    private final CoachRepository coachRepository;

    public List<AchievementResponse> getAllAchievements() {
        return achievementRepository.findAll().stream()
                .map(this::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    public AchievementResponse getAchievementById(Long id) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + id));
        return mapToAchievementResponse(achievement);
    }

    public List<AchievementResponse> getAchievementsByStudent(Long studentId) {
        return achievementRepository.findByStudentId(studentId).stream()
                .map(this::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    public List<AchievementResponse> getVerifiedAchievementsByStudent(Long studentId) {
        return achievementRepository.findVerifiedAchievementsByStudentId(studentId).stream()
                .map(this::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    public List<AchievementResponse> getAchievementsByType(AchievementType type) {
        return achievementRepository.findByType(type).stream()
                .map(this::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    public List<AchievementResponse> getAchievementsByStudentAndType(Long studentId, AchievementType type) {
        return achievementRepository.findByStudentIdAndType(studentId, type).stream()
                .map(this::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    public List<AchievementResponse> getAchievementsByDateRange(LocalDate startDate, LocalDate endDate) {
        return achievementRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    public List<AchievementResponse> getPendingVerificationAchievements() {
        return achievementRepository.findPendingVerificationAchievements().stream()
                .map(this::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AchievementResponse createAchievement(CreateAchievementRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Achievement achievement = Achievement.builder()
                .student(student)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .eventName(request.getEventName())
                .position(request.getPosition())
                .achievedDate(request.getAchievedDate())
                .certificateUrl(request.getCertificateUrl())
                .awardedBy(request.getAwardedBy())
                .isVerified(false)
                .build();

        Achievement savedAchievement = achievementRepository.save(achievement);
        log.info("Achievement created for student {}: {}", request.getStudentId(), request.getTitle());
        return mapToAchievementResponse(savedAchievement);
    }

    @Transactional
    public AchievementResponse updateAchievement(Long id, CreateAchievementRequest request) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + id));

        if (request.getTitle() != null) achievement.setTitle(request.getTitle());
        if (request.getDescription() != null) achievement.setDescription(request.getDescription());
        if (request.getType() != null) achievement.setType(request.getType());
        if (request.getEventName() != null) achievement.setEventName(request.getEventName());
        if (request.getPosition() != null) achievement.setPosition(request.getPosition());
        if (request.getAchievedDate() != null) achievement.setAchievedDate(request.getAchievedDate());
        if (request.getCertificateUrl() != null) achievement.setCertificateUrl(request.getCertificateUrl());
        if (request.getAwardedBy() != null) achievement.setAwardedBy(request.getAwardedBy());

        Achievement updatedAchievement = achievementRepository.save(achievement);
        log.info("Achievement updated: {}", id);
        return mapToAchievementResponse(updatedAchievement);
    }

    @Transactional
    public AchievementResponse verifyAchievement(Long achievementId, Long coachId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + achievementId));

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        achievement.setIsVerified(true);
        achievement.setVerifiedBy(coach);

        Achievement verifiedAchievement = achievementRepository.save(achievement);
        log.info("Achievement {} verified by coach {}", achievementId, coachId);
        return mapToAchievementResponse(verifiedAchievement);
    }

    @Transactional
    public AchievementResponse unverifyAchievement(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + achievementId));

        achievement.setIsVerified(false);
        achievement.setVerifiedBy(null);

        Achievement unverifiedAchievement = achievementRepository.save(achievement);
        log.info("Achievement {} unverified", achievementId);
        return mapToAchievementResponse(unverifiedAchievement);
    }

    @Transactional
    public void deleteAchievement(Long id) {
        if (!achievementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Achievement not found with id: " + id);
        }
        achievementRepository.deleteById(id);
        log.info("Achievement deleted with id: {}", id);
    }

    public Long countVerifiedAchievements(Long studentId) {
        return achievementRepository.countVerifiedAchievementsByStudentId(studentId);
    }

    private AchievementResponse mapToAchievementResponse(Achievement achievement) {
        return AchievementResponse.builder()
                .id(achievement.getId())
                .studentId(achievement.getStudent().getId())
                .studentName(achievement.getStudent().getFullName())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .type(achievement.getType())
                .eventName(achievement.getEventName())
                .position(achievement.getPosition())
                .achievedDate(achievement.getAchievedDate())
                .certificateUrl(achievement.getCertificateUrl())
                .awardedBy(achievement.getAwardedBy())
                .isVerified(achievement.getIsVerified())
                .verifiedById(achievement.getVerifiedBy() != null ? achievement.getVerifiedBy().getId() : null)
                .verifiedByName(achievement.getVerifiedBy() != null ? achievement.getVerifiedBy().getFullName() : null)
                .build();
    }
}
