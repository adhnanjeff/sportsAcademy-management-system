package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateAchievementRequest;
import com.badminton.academy.dto.response.AchievementResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Achievement;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.Student;
import com.badminton.academy.model.User;
import com.badminton.academy.model.enums.AchievementType;
import com.badminton.academy.model.enums.ActivityAction;
import com.badminton.academy.model.enums.EntityType;
import com.badminton.academy.repository.AchievementRepository;
import com.badminton.academy.repository.CoachRepository;
import com.badminton.academy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final S3Service s3Service;
    private final ActivityLogService activityLogService;

    public List<AchievementResponse> getAllAchievements() {
        return achievementRepository.findAllWithStudentAndCoach().stream()
                .map(this::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    public AchievementResponse getAchievementById(Long id) {
        Achievement achievement = achievementRepository.findByIdWithDetails(id);
        if (achievement == null) {
            throw new ResourceNotFoundException("Achievement not found with id: " + id);
        }
        return mapToAchievementResponse(achievement);
    }

    public List<AchievementResponse> getAchievementsByStudent(Long studentId) {
        return achievementRepository.findByStudentIdWithDetails(studentId).stream()
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
    public AchievementResponse createAchievement(CreateAchievementRequest request, MultipartFile certificate) {
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
                .awardedBy(request.getAwardedBy())
                .isVerified(false)
                .build();

        // Upload certificate if provided
        if (certificate != null && !certificate.isEmpty()) {
            String certificateKey = s3Service.uploadFileAndReturnKey(certificate, "achievements/certificates");
            achievement.setCertificateUrl(certificateKey);
            log.info("Certificate uploaded for achievement key: {}", certificateKey);
        } else if (request.getCertificateUrl() != null) {
            achievement.setCertificateUrl(request.getCertificateUrl());
        }

        Achievement savedAchievement = achievementRepository.save(achievement);
        log.info("Achievement created for student {}: {}", request.getStudentId(), request.getTitle());
        return mapToAchievementResponse(savedAchievement);
    }

    @Transactional
    public AchievementResponse updateAchievement(Long id, CreateAchievementRequest request, MultipartFile newCertificate) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + id));

        if (request.getTitle() != null) achievement.setTitle(request.getTitle());
        if (request.getDescription() != null) achievement.setDescription(request.getDescription());
        if (request.getType() != null) achievement.setType(request.getType());
        if (request.getEventName() != null) achievement.setEventName(request.getEventName());
        if (request.getPosition() != null) achievement.setPosition(request.getPosition());
        if (request.getAchievedDate() != null) achievement.setAchievedDate(request.getAchievedDate());
        if (request.getAwardedBy() != null) achievement.setAwardedBy(request.getAwardedBy());

        // Handle certificate update
        if (newCertificate != null && !newCertificate.isEmpty()) {
            String newCertificateKey = s3Service.replaceFile(
                    achievement.getCertificateUrl(),
                    newCertificate,
                    "achievements/certificates"
            );
            achievement.setCertificateUrl(newCertificateKey);
            log.info("Certificate updated for achievement {} key: {}", id, newCertificateKey);
        } else if (request.getCertificateUrl() != null) {
            achievement.setCertificateUrl(request.getCertificateUrl());
        }

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
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + id));

        // Delete certificate from S3 if exists
        if (achievement.getCertificateUrl() != null && !achievement.getCertificateUrl().isEmpty()) {
            try {
                s3Service.deleteFile(achievement.getCertificateUrl());
                log.info("Deleted certificate from S3 for achievement id: {}", id);
            } catch (Exception e) {
                log.error("Failed to delete certificate from S3: {}", e.getMessage());
            }
        }

        achievementRepository.deleteById(id);
        log.info("Achievement deleted with id: {}", id);
    }

    @Transactional
    public void deleteCertificate(Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found with id: " + achievementId));

        if (achievement.getCertificateUrl() != null && !achievement.getCertificateUrl().isEmpty()) {
            s3Service.deleteFile(achievement.getCertificateUrl());
            achievement.setCertificateUrl(null);
            achievementRepository.save(achievement);
            log.info("Deleted certificate for achievement id: {}", achievementId);
        }
    }

    public Long countVerifiedAchievements(Long studentId) {
        return achievementRepository.countVerifiedAchievementsByStudentId(studentId);
    }

    /**
     * Bulk delete achievements
     */
    @Transactional
    public void bulkDeleteAchievements(List<Long> achievementIds, User currentUser) {
        log.info("Bulk deleting {} achievements", achievementIds.size());
        
        List<Achievement> achievements = achievementRepository.findAllById(achievementIds);
        
        // Delete certificate files from S3
        for (Achievement achievement : achievements) {
            if (achievement.getCertificateUrl() != null) {
                try {
                    s3Service.deleteFile(achievement.getCertificateUrl());
                } catch (Exception e) {
                    log.warn("Failed to delete certificate for achievement {}: {}", achievement.getId(), e.getMessage());
                }
            }
        }
        
        // Delete achievements
        achievementRepository.deleteAllById(achievementIds);
        
        // Log activity
        activityLogService.logActivity(
            ActivityAction.BULK_DELETE,
            EntityType.ACHIEVEMENT,
            0L, // No specific entity ID for bulk operations
            String.format("Bulk deleted %d achievements", achievementIds.size()),
            currentUser
        );
        
        log.info("Successfully bulk deleted {} achievements", achievementIds.size());
    }

    /**
     * Bulk verify/unverify achievements
     */
    @Transactional
    public void bulkVerifyAchievements(List<Long> achievementIds, boolean verified, User currentUser) {
        log.info("Bulk {} {} achievements", verified ? "verifying" : "unverifying", achievementIds.size());
        
        List<Achievement> achievements = achievementRepository.findAllById(achievementIds);
        
        if (achievements.size() != achievementIds.size()) {
            log.warn("Some achievements not found. Requested: {}, Found: {}", 
                achievementIds.size(), achievements.size());
        }
        
        for (Achievement achievement : achievements) {
            achievement.setIsVerified(verified);
            if (verified && currentUser != null) {
                Coach coach = coachRepository.findById(currentUser.getId())
                    .orElse(null);
                achievement.setVerifiedBy(coach);
            } else if (!verified) {
                achievement.setVerifiedBy(null);
            }
        }
        
        achievementRepository.saveAll(achievements);
        
        // Log activity
        activityLogService.logActivity(
            ActivityAction.BULK_VERIFY,
            EntityType.ACHIEVEMENT,
            0L,
            String.format("Bulk %s %d achievements", 
                verified ? "verified" : "unverified", achievementIds.size()),
            currentUser
        );
        
        log.info("Successfully bulk {} {} achievements", 
            verified ? "verified" : "unverified", achievements.size());
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
                .certificateUrl(s3Service.resolveFileUrl(achievement.getCertificateUrl()))
                .awardedBy(achievement.getAwardedBy())
                .isVerified(achievement.getIsVerified())
                .verifiedById(achievement.getVerifiedBy() != null ? achievement.getVerifiedBy().getId() : null)
                .verifiedByName(achievement.getVerifiedBy() != null ? achievement.getVerifiedBy().getFullName() : null)
                .build();
    }
}
