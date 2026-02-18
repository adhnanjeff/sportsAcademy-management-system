package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateAssessmentRequest;
import com.badminton.academy.dto.response.AssessmentResponse;
import com.badminton.academy.model.enums.AssessmentType;
import java.util.List;

public interface IAssessmentService {
    AssessmentResponse createAssessment(CreateAssessmentRequest request, Long coachId);
    AssessmentResponse getAssessmentById(Long id);
    List<AssessmentResponse> getAssessmentsByStudentId(Long studentId);
    List<AssessmentResponse> getAssessmentsByType(AssessmentType type);
    List<AssessmentResponse> getAllAssessments();
    AssessmentResponse updateAssessment(Long id, CreateAssessmentRequest request);
    void deleteAssessment(Long id);
}
