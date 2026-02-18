package com.badminton.academy.mapper;

import com.badminton.academy.dto.request.UpdateCoachRequest;
import com.badminton.academy.dto.response.CoachResponse;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Coach;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Collectors;

@Component
public class CoachMapper {

    public CoachResponse toResponse(Coach coach) {
        if (coach == null) return null;

        CoachResponse response = new CoachResponse();
        // Map inherited User fields
        response.setId(coach.getId());
        response.setEmail(coach.getEmail());
        response.setFirstName(coach.getFirstName());
        response.setLastName(coach.getLastName());
        response.setFullName(coach.getFullName());
        response.setNationalIdNumber(coach.getNationalIdNumber());
        response.setDateOfBirth(coach.getDateOfBirth());
        response.setAge(calculateAge(coach.getDateOfBirth()));
        response.setPhotoUrl(coach.getPhotoUrl());
        response.setPhoneNumber(coach.getPhoneNumber());
        response.setAddress(coach.getAddress());
        response.setCity(coach.getCity());
        response.setState(coach.getState());
        response.setCountry(coach.getCountry());
        response.setRole(coach.getRole());
        response.setIsActive(coach.getIsActive());
        response.setIsEmailVerified(coach.getIsEmailVerified());
        response.setCreatedAt(coach.getCreatedAt());
        response.setUpdatedAt(coach.getUpdatedAt());

        // Map Coach-specific fields
        response.setSpecialization(coach.getSpecialization());
        response.setYearsOfExperience(coach.getYearsOfExperience());
        response.setBio(coach.getBio());
        response.setCertifications(coach.getCertifications());

        if (coach.getBatches() != null) {
            response.setBatchIds(coach.getBatches().stream()
                    .map(Batch::getId)
                    .collect(Collectors.toSet()));
            response.setTotalBatches(coach.getBatches().size());
            response.setTotalStudents(calculateTotalStudents(coach));
        } else {
            response.setTotalBatches(0);
            response.setTotalStudents(0);
        }

        return response;
    }

    public void updateEntityFromRequest(UpdateCoachRequest request, Coach coach) {
        if (request == null || coach == null) return;

        if (request.getEmail() != null) coach.setEmail(request.getEmail());
        if (request.getFirstName() != null) coach.setFirstName(request.getFirstName());
        if (request.getLastName() != null) coach.setLastName(request.getLastName());
        if (request.getFirstName() != null || request.getLastName() != null) {
            coach.setFullName(coach.getFirstName() + " " + coach.getLastName());
        }
        if (request.getDateOfBirth() != null) coach.setDateOfBirth(request.getDateOfBirth());
        if (request.getPhoneNumber() != null) coach.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) coach.setAddress(request.getAddress());
        if (request.getCity() != null) coach.setCity(request.getCity());
        if (request.getState() != null) coach.setState(request.getState());
        if (request.getCountry() != null) coach.setCountry(request.getCountry());
        if (request.getPhotoUrl() != null) coach.setPhotoUrl(request.getPhotoUrl());
        if (request.getSpecialization() != null) coach.setSpecialization(request.getSpecialization());
        if (request.getYearsOfExperience() != null) coach.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getBio() != null) coach.setBio(request.getBio());
        if (request.getCertifications() != null) coach.setCertifications(request.getCertifications());
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private Integer calculateTotalStudents(Coach coach) {
        if (coach.getBatches() == null) return 0;
        return coach.getBatches().stream()
                .mapToInt(batch -> batch.getStudents() != null ? batch.getStudents().size() : 0)
                .sum();
    }
}
