package com.badminton.academy.mapper;

import com.badminton.academy.dto.request.CreateAssessmentRequest;
import com.badminton.academy.dto.response.AssessmentResponse;
import com.badminton.academy.model.Assessment;
import org.springframework.stereotype.Component;

@Component
public class AssessmentMapper {

    public AssessmentResponse toResponse(Assessment assessment) {
        if (assessment == null) return null;

        AssessmentResponse response = new AssessmentResponse();
        response.setId(assessment.getId());
        response.setType(assessment.getType());
        response.setName(assessment.getName());
        response.setScore(assessment.getScore());
        response.setUnit(assessment.getUnit());
        response.setTargetScore(assessment.getTargetScore());
        response.setAssessmentDate(assessment.getAssessmentDate());
        response.setNotes(assessment.getNotes());
        response.setCreatedAt(assessment.getCreatedAt());
        response.setTargetAchieved(calculateTargetAchieved(assessment));

        if (assessment.getStudent() != null) {
            response.setStudentId(assessment.getStudent().getId());
            response.setStudentName(assessment.getStudent().getFullName());
        }

        if (assessment.getConductedBy() != null) {
            response.setConductedById(assessment.getConductedBy().getId());
            response.setConductedByName(assessment.getConductedBy().getFullName());
        }

        return response;
    }

    public Assessment toEntity(CreateAssessmentRequest request) {
        if (request == null) return null;

        Assessment assessment = new Assessment();
        assessment.setType(request.getType());
        assessment.setName(request.getName());
        assessment.setScore(request.getScore());
        assessment.setUnit(request.getUnit());
        assessment.setTargetScore(request.getTargetScore());
        assessment.setAssessmentDate(request.getAssessmentDate());
        assessment.setNotes(request.getNotes());
        return assessment;
    }

    public void updateEntityFromRequest(CreateAssessmentRequest request, Assessment assessment) {
        if (request == null || assessment == null) return;

        if (request.getType() != null) assessment.setType(request.getType());
        if (request.getName() != null) assessment.setName(request.getName());
        if (request.getScore() != null) assessment.setScore(request.getScore());
        if (request.getUnit() != null) assessment.setUnit(request.getUnit());
        if (request.getTargetScore() != null) assessment.setTargetScore(request.getTargetScore());
        if (request.getAssessmentDate() != null) assessment.setAssessmentDate(request.getAssessmentDate());
        if (request.getNotes() != null) assessment.setNotes(request.getNotes());
    }

    private Boolean calculateTargetAchieved(Assessment assessment) {
        if (assessment.getTargetScore() == null || assessment.getScore() == null) {
            return null;
        }
        return assessment.getScore() >= assessment.getTargetScore();
    }
}
