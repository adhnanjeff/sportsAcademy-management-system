package com.badminton.academy.mapper;

import com.badminton.academy.dto.request.UpdateStudentRequest;
import com.badminton.academy.dto.response.StudentResponse;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Student;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class StudentMapper {

    public StudentResponse toResponse(Student student) {
        if (student == null) return null;

        StudentResponse response = new StudentResponse();
        // Map Student fields (no longer inherited from User)
        response.setId(student.getId());
        response.setFirstName(student.getFirstName());
        response.setLastName(student.getLastName());
        response.setFullName(student.getFullName());
        response.setGender(student.getGender());
        response.setNationalIdNumber(student.getNationalIdNumber());
        response.setDateOfBirth(student.getDateOfBirth());
        response.setAge(calculateAge(student.getDateOfBirth()));
        response.setPhotoUrl(student.getPhotoUrl());
        response.setPhoneNumber(student.getPhoneNumber());
        response.setAddress(student.getAddress());
        response.setCity(student.getCity());
        response.setState(student.getState());
        response.setCountry(student.getCountry());
        response.setIsActive(student.getIsActive());
        response.setCreatedAt(student.getCreatedAt());
        response.setUpdatedAt(student.getUpdatedAt());

        // Map Student-specific fields
        response.setSkillLevel(student.getSkillLevel());
        response.setDaysOfWeek(student.getDaysOfWeek() != null
                ? new HashSet<>(student.getDaysOfWeek())
                : new HashSet<>());

        if (student.getParent() != null) {
            response.setParentId(student.getParent().getId());
            response.setParentName(student.getParent().getFullName());
        }

        if (student.getBatches() != null) {
            response.setBatchIds(student.getBatches().stream()
                    .map(Batch::getId)
                    .collect(Collectors.toSet()));
            response.setBatchNames(student.getBatches().stream()
                    .map(Batch::getName)
                    .collect(Collectors.toSet()));
            response.setTotalBatches(student.getBatches().size());
        } else {
            response.setTotalBatches(0);
        }

        // These can be set in the service layer with actual calculations
        response.setTotalAchievements(0);
        response.setAttendancePercentage(0.0);
        response.setAverageSkillRating(0.0);
        response.setFeePayable(student.getFeePayable());
        response.setMonthlyFeeStatus(student.getMonthlyFeeStatus());

        return response;
    }

    public void updateEntityFromRequest(UpdateStudentRequest request, Student student) {
        if (request == null || student == null) return;

        if (request.getFirstName() != null) student.setFirstName(request.getFirstName());
        if (request.getLastName() != null) student.setLastName(request.getLastName());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getFirstName() != null || request.getLastName() != null) {
            student.setFullName(student.getFirstName() + " " + student.getLastName());
        }
        if (request.getNationalIdNumber() != null) student.setNationalIdNumber(request.getNationalIdNumber());
        if (request.getDateOfBirth() != null) student.setDateOfBirth(request.getDateOfBirth());
        if (request.getPhoneNumber() != null) student.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) student.setAddress(request.getAddress());
        if (request.getCity() != null) student.setCity(request.getCity());
        if (request.getState() != null) student.setState(request.getState());
        if (request.getCountry() != null) student.setCountry(request.getCountry());
        if (request.getPhotoUrl() != null) student.setPhotoUrl(request.getPhotoUrl());
        if (request.getSkillLevel() != null) student.setSkillLevel(request.getSkillLevel());
        if (request.getDaysOfWeek() != null) student.setDaysOfWeek(request.getDaysOfWeek());
        if (request.getFeePayable() != null) student.setFeePayable(request.getFeePayable());
        if (request.getMonthlyFeeStatus() != null) student.setMonthlyFeeStatus(request.getMonthlyFeeStatus());
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
