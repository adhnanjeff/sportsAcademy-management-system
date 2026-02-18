package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateSkillEvaluationRequest;
import com.badminton.academy.dto.response.SkillEvaluationResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.SkillEvaluation;
import com.badminton.academy.model.Student;
import com.badminton.academy.repository.CoachRepository;
import com.badminton.academy.repository.SkillEvaluationRepository;
import com.badminton.academy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillEvaluationService {

    private final SkillEvaluationRepository skillEvaluationRepository;
    private final StudentRepository studentRepository;
    private final CoachRepository coachRepository;

    public List<SkillEvaluationResponse> getAllSkillEvaluations() {
        return skillEvaluationRepository.findAll().stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    public SkillEvaluationResponse getSkillEvaluationById(Long id) {
        SkillEvaluation evaluation = skillEvaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill evaluation not found with id: " + id));
        return mapToSkillEvaluationResponse(evaluation);
    }

    public List<SkillEvaluationResponse> getSkillEvaluationsByStudent(Long studentId) {
        return skillEvaluationRepository.findByStudentId(studentId).stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    public List<SkillEvaluationResponse> getSkillEvaluationsByStudentOrderByDate(Long studentId) {
        return skillEvaluationRepository.findByStudentIdOrderByDateDesc(studentId).stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    public Optional<SkillEvaluationResponse> getLatestSkillEvaluation(Long studentId) {
        return skillEvaluationRepository.findLatestByStudentId(studentId)
                .map(this::mapToSkillEvaluationResponse);
    }

    public List<SkillEvaluationResponse> getSkillEvaluationsByCoach(Long coachId) {
        return skillEvaluationRepository.findByEvaluatedById(coachId).stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    public List<SkillEvaluationResponse> getSkillEvaluationsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return skillEvaluationRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    public List<SkillEvaluationResponse> getSkillEvaluationsByStudentAndDateRange(
            Long studentId, LocalDateTime startDate, LocalDateTime endDate) {
        return skillEvaluationRepository.findByStudentIdAndDateRange(studentId, startDate, endDate).stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    public List<SkillEvaluationResponse> getRecentEvaluationsByCoach(Long coachId, LocalDateTime since) {
        return skillEvaluationRepository.findByCoachIdSince(coachId, since).stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SkillEvaluationResponse createSkillEvaluation(CreateSkillEvaluationRequest request, Long coachId) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        SkillEvaluation evaluation = SkillEvaluation.builder()
                .student(student)
                .evaluatedBy(coach)
                .footwork(request.getFootwork())
                .strokes(request.getStrokes())
                .stamina(request.getStamina())
                .attack(request.getAttack())
                .defence(request.getDefence())
                .agility(request.getAgility())
                .courtCoverage(request.getCourtCoverage())
                .notes(request.getNotes())
                .build();

        SkillEvaluation savedEvaluation = skillEvaluationRepository.save(evaluation);
        log.info("Skill evaluation created for student {} by coach {}", request.getStudentId(), coachId);
        return mapToSkillEvaluationResponse(savedEvaluation);
    }

    @Transactional
    public SkillEvaluationResponse updateSkillEvaluation(Long id, CreateSkillEvaluationRequest request) {
        SkillEvaluation evaluation = skillEvaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill evaluation not found with id: " + id));

        if (request.getFootwork() != null) evaluation.setFootwork(request.getFootwork());
        if (request.getStrokes() != null) evaluation.setStrokes(request.getStrokes());
        if (request.getStamina() != null) evaluation.setStamina(request.getStamina());
        if (request.getAttack() != null) evaluation.setAttack(request.getAttack());
        if (request.getDefence() != null) evaluation.setDefence(request.getDefence());
        if (request.getAgility() != null) evaluation.setAgility(request.getAgility());
        if (request.getCourtCoverage() != null) evaluation.setCourtCoverage(request.getCourtCoverage());
        if (request.getNotes() != null) evaluation.setNotes(request.getNotes());

        SkillEvaluation updatedEvaluation = skillEvaluationRepository.save(evaluation);
        log.info("Skill evaluation updated: {}", id);
        return mapToSkillEvaluationResponse(updatedEvaluation);
    }

    @Transactional
    public void deleteSkillEvaluation(Long id) {
        if (!skillEvaluationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill evaluation not found with id: " + id);
        }
        skillEvaluationRepository.deleteById(id);
        log.info("Skill evaluation deleted with id: {}", id);
    }

    public Long countByStudent(Long studentId) {
        return skillEvaluationRepository.countByStudentId(studentId);
    }

    public Long countByCoach(Long coachId) {
        return skillEvaluationRepository.countByCoachId(coachId);
    }

    public Double getAverageScore(Long studentId) {
        return skillEvaluationRepository.getAverageOverallScoreByStudentId(studentId);
    }

    private SkillEvaluationResponse mapToSkillEvaluationResponse(SkillEvaluation evaluation) {
        Double averageScore = (evaluation.getFootwork() + evaluation.getStrokes() + 
                evaluation.getStamina() + evaluation.getAttack() + 
                evaluation.getDefence() + evaluation.getAgility() + 
                evaluation.getCourtCoverage()) / 7.0;

        return SkillEvaluationResponse.builder()
                .id(evaluation.getId())
                .studentId(evaluation.getStudent().getId())
                .studentName(evaluation.getStudent().getFullName())
                .evaluatedById(evaluation.getEvaluatedBy().getId())
                .evaluatedByName(evaluation.getEvaluatedBy().getFullName())
                .footwork(evaluation.getFootwork())
                .strokes(evaluation.getStrokes())
                .stamina(evaluation.getStamina())
                .attack(evaluation.getAttack())
                .defence(evaluation.getDefence())
                .agility(evaluation.getAgility())
                .courtCoverage(evaluation.getCourtCoverage())
                .notes(evaluation.getNotes())
                .evaluatedAt(evaluation.getEvaluatedAt())
                .averageScore(averageScore)
                .build();
    }
}
