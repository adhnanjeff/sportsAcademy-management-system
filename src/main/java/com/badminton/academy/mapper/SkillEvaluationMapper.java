package com.badminton.academy.mapper;

import com.badminton.academy.dto.request.CreateSkillEvaluationRequest;
import com.badminton.academy.dto.response.SkillEvaluationResponse;
import com.badminton.academy.model.SkillEvaluation;
import org.springframework.stereotype.Component;

@Component
public class SkillEvaluationMapper {

    public SkillEvaluationResponse toResponse(SkillEvaluation evaluation) {
        if (evaluation == null) return null;

        SkillEvaluationResponse response = new SkillEvaluationResponse();
        response.setId(evaluation.getId());
        response.setFootwork(evaluation.getFootwork());
        response.setStrokes(evaluation.getStrokes());
        response.setStamina(evaluation.getStamina());
        response.setAttack(evaluation.getAttack());
        response.setDefence(evaluation.getDefence());
        response.setAgility(evaluation.getAgility());
        response.setCourtCoverage(evaluation.getCourtCoverage());
        response.setNotes(evaluation.getNotes());
        response.setEvaluatedAt(evaluation.getEvaluatedAt());
        response.setAverageScore(calculateAverageScore(evaluation));

        if (evaluation.getStudent() != null) {
            response.setStudentId(evaluation.getStudent().getId());
            response.setStudentName(evaluation.getStudent().getFullName());
        }

        if (evaluation.getEvaluatedBy() != null) {
            response.setEvaluatedById(evaluation.getEvaluatedBy().getId());
            response.setEvaluatedByName(evaluation.getEvaluatedBy().getFullName());
        }

        return response;
    }

    public SkillEvaluation toEntity(CreateSkillEvaluationRequest request) {
        if (request == null) return null;

        SkillEvaluation evaluation = new SkillEvaluation();
        evaluation.setFootwork(request.getFootwork());
        evaluation.setStrokes(request.getStrokes());
        evaluation.setStamina(request.getStamina());
        evaluation.setAttack(request.getAttack());
        evaluation.setDefence(request.getDefence());
        evaluation.setAgility(request.getAgility());
        evaluation.setCourtCoverage(request.getCourtCoverage());
        evaluation.setNotes(request.getNotes());
        return evaluation;
    }

    public void updateEntityFromRequest(CreateSkillEvaluationRequest request, SkillEvaluation evaluation) {
        if (request == null || evaluation == null) return;

        if (request.getFootwork() != null) evaluation.setFootwork(request.getFootwork());
        if (request.getStrokes() != null) evaluation.setStrokes(request.getStrokes());
        if (request.getStamina() != null) evaluation.setStamina(request.getStamina());
        if (request.getAttack() != null) evaluation.setAttack(request.getAttack());
        if (request.getDefence() != null) evaluation.setDefence(request.getDefence());
        if (request.getAgility() != null) evaluation.setAgility(request.getAgility());
        if (request.getCourtCoverage() != null) evaluation.setCourtCoverage(request.getCourtCoverage());
        if (request.getNotes() != null) evaluation.setNotes(request.getNotes());
    }

    private Double calculateAverageScore(SkillEvaluation evaluation) {
        if (evaluation == null) return null;
        return (evaluation.getFootwork() + evaluation.getStrokes() +
                evaluation.getStamina() + evaluation.getAttack() +
                evaluation.getDefence() + evaluation.getAgility() +
                evaluation.getCourtCoverage()) / 7.0;
    }
}
