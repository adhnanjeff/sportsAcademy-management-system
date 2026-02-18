package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateSkillEvaluationRequest;
import com.badminton.academy.dto.response.SkillEvaluationResponse;
import java.util.List;

public interface ISkillEvaluationService {
    SkillEvaluationResponse createEvaluation(CreateSkillEvaluationRequest request, Long coachId);
    SkillEvaluationResponse getEvaluationById(Long id);
    List<SkillEvaluationResponse> getEvaluationsByStudentId(Long studentId);
    SkillEvaluationResponse getLatestEvaluationByStudentId(Long studentId);
    List<SkillEvaluationResponse> getAllEvaluations();
    SkillEvaluationResponse updateEvaluation(Long id, CreateSkillEvaluationRequest request);
    void deleteEvaluation(Long id);
}
