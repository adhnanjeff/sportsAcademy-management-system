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
        response.setSmashPower(evaluation.getSmashPower());
        response.setNetControl(evaluation.getNetControl());
        response.setBackhand(evaluation.getBackhand());
        response.setFootwork(evaluation.getFootwork());
        response.setAgility(evaluation.getAgility());
        response.setStamina(evaluation.getStamina());
        response.setTacticalAwareness(evaluation.getTacticalAwareness());
        response.setMentalStrength(evaluation.getMentalStrength());
        response.setNotes(evaluation.getNotes());
        response.setEvaluatedAt(evaluation.getEvaluatedAt());
        response.setMonth(evaluation.getMonth());
        response.setYear(evaluation.getYear());
        response.setAverageScore(calculateAverageScore(evaluation));

        if (evaluation.getStudent() != null) {
            response.setStudentId(evaluation.getStudent().getId());
            response.setStudentName(evaluation.getStudent().getFullName());
            // Get first batch if available
            if (evaluation.getStudent().getBatches() != null && !evaluation.getStudent().getBatches().isEmpty()) {
                var batch = evaluation.getStudent().getBatches().iterator().next();
                response.setBatchId(batch.getId());
                response.setBatchName(batch.getName());
            }
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
        evaluation.setSmashPower(request.getSmashPower());
        evaluation.setNetControl(request.getNetControl());
        evaluation.setBackhand(request.getBackhand());
        evaluation.setFootwork(request.getFootwork());
        evaluation.setAgility(request.getAgility());
        evaluation.setStamina(request.getStamina());
        evaluation.setTacticalAwareness(request.getTacticalAwareness());
        evaluation.setMentalStrength(request.getMentalStrength());
        evaluation.setNotes(request.getNotes());
        return evaluation;
    }

    public void updateEntityFromRequest(CreateSkillEvaluationRequest request, SkillEvaluation evaluation) {
        if (request == null || evaluation == null) return;

        if (request.getSmashPower() != null) evaluation.setSmashPower(request.getSmashPower());
        if (request.getNetControl() != null) evaluation.setNetControl(request.getNetControl());
        if (request.getBackhand() != null) evaluation.setBackhand(request.getBackhand());
        if (request.getFootwork() != null) evaluation.setFootwork(request.getFootwork());
        if (request.getAgility() != null) evaluation.setAgility(request.getAgility());
        if (request.getStamina() != null) evaluation.setStamina(request.getStamina());
        if (request.getTacticalAwareness() != null) evaluation.setTacticalAwareness(request.getTacticalAwareness());
        if (request.getMentalStrength() != null) evaluation.setMentalStrength(request.getMentalStrength());
        if (request.getNotes() != null) evaluation.setNotes(request.getNotes());
    }

    private Double calculateAverageScore(SkillEvaluation evaluation) {
        if (evaluation == null) return null;
        return (evaluation.getSmashPower() + evaluation.getNetControl() +
                evaluation.getBackhand() + evaluation.getFootwork() +
                evaluation.getAgility() + evaluation.getStamina() +
                evaluation.getTacticalAwareness() + evaluation.getMentalStrength()) / 8.0;
    }
}
