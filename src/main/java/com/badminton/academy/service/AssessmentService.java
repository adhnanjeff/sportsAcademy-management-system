package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateAssessmentRequest;
import com.badminton.academy.dto.response.AssessmentResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Assessment;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.Student;
import com.badminton.academy.model.enums.AssessmentType;
import com.badminton.academy.repository.AssessmentRepository;
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
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final CoachRepository coachRepository;

    public List<AssessmentResponse> getAllAssessments() {
        return assessmentRepository.findAll().stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public AssessmentResponse getAssessmentById(Long id) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
        return mapToAssessmentResponse(assessment);
    }

    public List<AssessmentResponse> getAssessmentsByStudent(Long studentId) {
        return assessmentRepository.findByStudentId(studentId).stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public List<AssessmentResponse> getAssessmentsByStudentOrderByDate(Long studentId) {
        return assessmentRepository.findByStudentIdOrderByDateDesc(studentId).stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public List<AssessmentResponse> getAssessmentsByCoach(Long coachId) {
        return assessmentRepository.findByConductedById(coachId).stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public List<AssessmentResponse> getAssessmentsByType(AssessmentType type) {
        return assessmentRepository.findByType(type).stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public List<AssessmentResponse> getAssessmentsByStudentAndType(Long studentId, AssessmentType type) {
        return assessmentRepository.findByStudentIdAndType(studentId, type).stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public List<AssessmentResponse> getAssessmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return assessmentRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public List<AssessmentResponse> getAssessmentsByStudentAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        return assessmentRepository.findByStudentIdAndDateRange(studentId, startDate, endDate).stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    public List<AssessmentResponse> getAssessmentProgress(Long studentId, String assessmentName) {
        return assessmentRepository.findByStudentIdAndNameOrderByDateDesc(studentId, assessmentName).stream()
                .map(this::mapToAssessmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssessmentResponse createAssessment(CreateAssessmentRequest request, Long coachId) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        Assessment assessment = Assessment.builder()
                .student(student)
                .conductedBy(coach)
                .type(request.getType())
                .name(request.getName())
                .score(request.getScore())
                .unit(request.getUnit())
                .targetScore(request.getTargetScore())
                .assessmentDate(request.getAssessmentDate())
                .notes(request.getNotes())
                .build();

        Assessment savedAssessment = assessmentRepository.save(assessment);
        log.info("Assessment created for student {}: {}", request.getStudentId(), request.getName());
        return mapToAssessmentResponse(savedAssessment);
    }

    @Transactional
    public AssessmentResponse updateAssessment(Long id, CreateAssessmentRequest request, Long coachId) {
        Assessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));

        if (request.getType() != null) assessment.setType(request.getType());
        if (request.getName() != null) assessment.setName(request.getName());
        if (request.getScore() != null) assessment.setScore(request.getScore());
        if (request.getUnit() != null) assessment.setUnit(request.getUnit());
        if (request.getTargetScore() != null) assessment.setTargetScore(request.getTargetScore());
        if (request.getAssessmentDate() != null) assessment.setAssessmentDate(request.getAssessmentDate());
        if (request.getNotes() != null) assessment.setNotes(request.getNotes());

        Assessment updatedAssessment = assessmentRepository.save(assessment);
        log.info("Assessment updated: {}", id);
        return mapToAssessmentResponse(updatedAssessment);
    }

    @Transactional
    public void deleteAssessment(Long id) {
        if (!assessmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assessment not found with id: " + id);
        }
        assessmentRepository.deleteById(id);
        log.info("Assessment deleted with id: {}", id);
    }

    public Long countByCoach(Long coachId) {
        return assessmentRepository.countByCoachId(coachId);
    }

    private AssessmentResponse mapToAssessmentResponse(Assessment assessment) {
        Boolean targetAchieved = null;
        if (assessment.getTargetScore() != null && assessment.getScore() != null) {
            targetAchieved = assessment.getScore() >= assessment.getTargetScore();
        }

        return AssessmentResponse.builder()
                .id(assessment.getId())
                .studentId(assessment.getStudent().getId())
                .studentName(assessment.getStudent().getFullName())
                .conductedById(assessment.getConductedBy().getId())
                .conductedByName(assessment.getConductedBy().getFullName())
                .type(assessment.getType())
                .name(assessment.getName())
                .score(assessment.getScore())
                .unit(assessment.getUnit())
                .targetScore(assessment.getTargetScore())
                .assessmentDate(assessment.getAssessmentDate())
                .notes(assessment.getNotes())
                .createdAt(assessment.getCreatedAt())
                .targetAchieved(targetAchieved)
                .build();
    }
}
