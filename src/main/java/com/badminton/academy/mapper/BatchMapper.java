package com.badminton.academy.mapper;

import com.badminton.academy.dto.request.CreateBatchRequest;
import com.badminton.academy.dto.request.UpdateBatchRequest;
import com.badminton.academy.dto.response.BatchResponse;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Student;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BatchMapper {

    public BatchResponse toResponse(Batch batch) {
        if (batch == null) return null;

        BatchResponse response = new BatchResponse();
        response.setId(batch.getId());
        response.setName(batch.getName());
        response.setSkillLevel(batch.getSkillLevel());
        response.setStartTime(batch.getStartTime());
        response.setEndTime(batch.getEndTime());
        response.setIsActive(batch.getIsActive());

        if (batch.getCoach() != null) {
            response.setCoachId(batch.getCoach().getId());
            response.setCoachName(batch.getCoach().getFullName());
        }

        if (batch.getStudents() != null) {
            response.setStudentIds(batch.getStudents().stream()
                    .map(Student::getId)
                    .collect(Collectors.toSet()));
            response.setTotalStudents(batch.getStudents().size());
        } else {
            response.setTotalStudents(0);
        }

        return response;
    }

    public Batch toEntity(CreateBatchRequest request) {
        if (request == null) return null;

        Batch batch = new Batch();
        batch.setName(request.getName());
        batch.setSkillLevel(request.getSkillLevel());
        batch.setStartTime(request.getStartTime());
        batch.setEndTime(request.getEndTime());
        batch.setIsActive(true);
        return batch;
    }

    public void updateEntityFromRequest(UpdateBatchRequest request, Batch batch) {
        if (request == null || batch == null) return;

        if (request.getName() != null) batch.setName(request.getName());
        if (request.getSkillLevel() != null) batch.setSkillLevel(request.getSkillLevel());
        if (request.getStartTime() != null) batch.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) batch.setEndTime(request.getEndTime());
        if (request.getIsActive() != null) batch.setIsActive(request.getIsActive());
    }
}
