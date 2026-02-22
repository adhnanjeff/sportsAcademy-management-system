package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateSkillEvaluationRequest;
import com.badminton.academy.dto.response.BatchAverageResponse;
import com.badminton.academy.dto.response.PerformanceProgressResponse;
import com.badminton.academy.dto.response.SkillEvaluationResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.SkillEvaluation;
import com.badminton.academy.model.Student;
import com.badminton.academy.repository.BatchRepository;
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
    private final BatchRepository batchRepository;

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
                .smashPower(request.getSmashPower())
                .netControl(request.getNetControl())
                .backhand(request.getBackhand())
                .footwork(request.getFootwork())
                .agility(request.getAgility())
                .stamina(request.getStamina())
                .tacticalAwareness(request.getTacticalAwareness())
                .mentalStrength(request.getMentalStrength())
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

        if (request.getSmashPower() != null) evaluation.setSmashPower(request.getSmashPower());
        if (request.getNetControl() != null) evaluation.setNetControl(request.getNetControl());
        if (request.getBackhand() != null) evaluation.setBackhand(request.getBackhand());
        if (request.getFootwork() != null) evaluation.setFootwork(request.getFootwork());
        if (request.getAgility() != null) evaluation.setAgility(request.getAgility());
        if (request.getStamina() != null) evaluation.setStamina(request.getStamina());
        if (request.getTacticalAwareness() != null) evaluation.setTacticalAwareness(request.getTacticalAwareness());
        if (request.getMentalStrength() != null) evaluation.setMentalStrength(request.getMentalStrength());
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

    public List<SkillEvaluationResponse> getEvaluationsByBatch(Long batchId) {
        return skillEvaluationRepository.findByBatchId(batchId).stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    public List<SkillEvaluationResponse> getLatestEvaluationsByBatch(Long batchId) {
        return skillEvaluationRepository.findLatestByBatchId(batchId).stream()
                .map(this::mapToSkillEvaluationResponse)
                .collect(Collectors.toList());
    }

    public BatchAverageResponse getBatchAverage(Long batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));
        
        List<SkillEvaluation> latestEvaluations = skillEvaluationRepository.findLatestByBatchId(batchId);
        
        if (latestEvaluations.isEmpty()) {
            return BatchAverageResponse.builder()
                    .batchId(batchId)
                    .batchName(batch.getName())
                    .totalPlayers(0)
                    .avgSmashPower(0.0)
                    .avgNetControl(0.0)
                    .avgBackhand(0.0)
                    .avgFootwork(0.0)
                    .avgAgility(0.0)
                    .avgStamina(0.0)
                    .avgTacticalAwareness(0.0)
                    .avgMentalStrength(0.0)
                    .overallAverage(0.0)
                    .build();
        }
        
        double avgSmashPower = latestEvaluations.stream().mapToInt(SkillEvaluation::getSmashPower).average().orElse(0);
        double avgNetControl = latestEvaluations.stream().mapToInt(SkillEvaluation::getNetControl).average().orElse(0);
        double avgBackhand = latestEvaluations.stream().mapToInt(SkillEvaluation::getBackhand).average().orElse(0);
        double avgFootwork = latestEvaluations.stream().mapToInt(SkillEvaluation::getFootwork).average().orElse(0);
        double avgAgility = latestEvaluations.stream().mapToInt(SkillEvaluation::getAgility).average().orElse(0);
        double avgStamina = latestEvaluations.stream().mapToInt(SkillEvaluation::getStamina).average().orElse(0);
        double avgTacticalAwareness = latestEvaluations.stream().mapToInt(SkillEvaluation::getTacticalAwareness).average().orElse(0);
        double avgMentalStrength = latestEvaluations.stream().mapToInt(SkillEvaluation::getMentalStrength).average().orElse(0);
        
        double overallAverage = (avgSmashPower + avgNetControl + avgBackhand + avgFootwork + 
                                  avgAgility + avgStamina + avgTacticalAwareness + avgMentalStrength) / 8.0;
        
        return BatchAverageResponse.builder()
                .batchId(batchId)
                .batchName(batch.getName())
                .totalPlayers(latestEvaluations.size())
                .avgSmashPower(Math.round(avgSmashPower * 10.0) / 10.0)
                .avgNetControl(Math.round(avgNetControl * 10.0) / 10.0)
                .avgBackhand(Math.round(avgBackhand * 10.0) / 10.0)
                .avgFootwork(Math.round(avgFootwork * 10.0) / 10.0)
                .avgAgility(Math.round(avgAgility * 10.0) / 10.0)
                .avgStamina(Math.round(avgStamina * 10.0) / 10.0)
                .avgTacticalAwareness(Math.round(avgTacticalAwareness * 10.0) / 10.0)
                .avgMentalStrength(Math.round(avgMentalStrength * 10.0) / 10.0)
                .overallAverage(Math.round(overallAverage * 10.0) / 10.0)
                .build();
    }

    public Optional<PerformanceProgressResponse> getPerformanceProgress(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        
        Optional<SkillEvaluation> firstEvaluation = skillEvaluationRepository.findFirstByStudentId(studentId);
        Optional<SkillEvaluation> latestEvaluation = skillEvaluationRepository.findLatestByStudentId(studentId);
        
        if (firstEvaluation.isEmpty() || latestEvaluation.isEmpty()) {
            return Optional.empty();
        }
        
        SkillEvaluation baseline = firstEvaluation.get();
        SkillEvaluation current = latestEvaluation.get();
        
        double baselineAvg = (baseline.getSmashPower() + baseline.getNetControl() + baseline.getBackhand() +
                              baseline.getFootwork() + baseline.getAgility() + baseline.getStamina() +
                              baseline.getTacticalAwareness() + baseline.getMentalStrength()) / 8.0;
        
        double currentAvg = (current.getSmashPower() + current.getNetControl() + current.getBackhand() +
                             current.getFootwork() + current.getAgility() + current.getStamina() +
                             current.getTacticalAwareness() + current.getMentalStrength()) / 8.0;
        
        double improvementPct = baselineAvg > 0 ? ((currentAvg - baselineAvg) / baselineAvg) * 100 : 0;
        
        return Optional.of(PerformanceProgressResponse.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .baselineSmashPower(baseline.getSmashPower())
                .baselineNetControl(baseline.getNetControl())
                .baselineBackhand(baseline.getBackhand())
                .baselineFootwork(baseline.getFootwork())
                .baselineAgility(baseline.getAgility())
                .baselineStamina(baseline.getStamina())
                .baselineTacticalAwareness(baseline.getTacticalAwareness())
                .baselineMentalStrength(baseline.getMentalStrength())
                .baselineDate(baseline.getEvaluatedAt())
                .baselineMonth(baseline.getMonth())
                .baselineYear(baseline.getYear())
                .currentSmashPower(current.getSmashPower())
                .currentNetControl(current.getNetControl())
                .currentBackhand(current.getBackhand())
                .currentFootwork(current.getFootwork())
                .currentAgility(current.getAgility())
                .currentStamina(current.getStamina())
                .currentTacticalAwareness(current.getTacticalAwareness())
                .currentMentalStrength(current.getMentalStrength())
                .currentDate(current.getEvaluatedAt())
                .currentMonth(current.getMonth())
                .currentYear(current.getYear())
                .improvementSmashPower(current.getSmashPower() - baseline.getSmashPower())
                .improvementNetControl(current.getNetControl() - baseline.getNetControl())
                .improvementBackhand(current.getBackhand() - baseline.getBackhand())
                .improvementFootwork(current.getFootwork() - baseline.getFootwork())
                .improvementAgility(current.getAgility() - baseline.getAgility())
                .improvementStamina(current.getStamina() - baseline.getStamina())
                .improvementTacticalAwareness(current.getTacticalAwareness() - baseline.getTacticalAwareness())
                .improvementMentalStrength(current.getMentalStrength() - baseline.getMentalStrength())
                .baselineAverage(Math.round(baselineAvg * 10.0) / 10.0)
                .currentAverage(Math.round(currentAvg * 10.0) / 10.0)
                .improvementPercentage(Math.round(improvementPct * 10.0) / 10.0)
                .build());
    }

    private SkillEvaluationResponse mapToSkillEvaluationResponse(SkillEvaluation evaluation) {
        Double averageScore = (evaluation.getSmashPower() + evaluation.getNetControl() + 
                evaluation.getBackhand() + evaluation.getFootwork() + 
                evaluation.getAgility() + evaluation.getStamina() + 
                evaluation.getTacticalAwareness() + evaluation.getMentalStrength()) / 8.0;

        // Get the first batch if student has any
        Long batchId = null;
        String batchName = null;
        if (evaluation.getStudent().getBatches() != null && !evaluation.getStudent().getBatches().isEmpty()) {
            var batch = evaluation.getStudent().getBatches().iterator().next();
            batchId = batch.getId();
            batchName = batch.getName();
        }

        return SkillEvaluationResponse.builder()
                .id(evaluation.getId())
                .studentId(evaluation.getStudent().getId())
                .studentName(evaluation.getStudent().getFullName())
                .batchId(batchId)
                .batchName(batchName)
                .evaluatedById(evaluation.getEvaluatedBy().getId())
                .evaluatedByName(evaluation.getEvaluatedBy().getFullName())
                .smashPower(evaluation.getSmashPower())
                .netControl(evaluation.getNetControl())
                .backhand(evaluation.getBackhand())
                .footwork(evaluation.getFootwork())
                .agility(evaluation.getAgility())
                .stamina(evaluation.getStamina())
                .tacticalAwareness(evaluation.getTacticalAwareness())
                .mentalStrength(evaluation.getMentalStrength())
                .notes(evaluation.getNotes())
                .evaluatedAt(evaluation.getEvaluatedAt())
                .month(evaluation.getMonth())
                .year(evaluation.getYear())
                .averageScore(averageScore)
                .build();
    }
}
