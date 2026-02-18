package com.badminton.academy.mapper;

import com.badminton.academy.dto.request.UpdateParentRequest;
import com.badminton.academy.dto.response.ParentResponse;
import com.badminton.academy.model.Parent;
import com.badminton.academy.model.Student;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Collectors;

@Component
public class ParentMapper {

    public ParentResponse toResponse(Parent parent) {
        if (parent == null) return null;

        ParentResponse response = new ParentResponse();
        // Map inherited User fields
        response.setId(parent.getId());
        response.setEmail(parent.getEmail());
        response.setFirstName(parent.getFirstName());
        response.setLastName(parent.getLastName());
        response.setFullName(parent.getFullName());
        response.setNationalIdNumber(parent.getNationalIdNumber());
        response.setDateOfBirth(parent.getDateOfBirth());
        response.setAge(calculateAge(parent.getDateOfBirth()));
        response.setPhotoUrl(parent.getPhotoUrl());
        response.setPhoneNumber(parent.getPhoneNumber());
        response.setAddress(parent.getAddress());
        response.setCity(parent.getCity());
        response.setState(parent.getState());
        response.setCountry(parent.getCountry());
        response.setRole(parent.getRole());
        response.setIsActive(parent.getIsActive());
        response.setIsEmailVerified(parent.getIsEmailVerified());
        response.setCreatedAt(parent.getCreatedAt());
        response.setUpdatedAt(parent.getUpdatedAt());

        // Map Parent-specific fields
        response.setParentPhoneNumber(parent.getPhoneNumber()); // Same as phoneNumber

        if (parent.getChildren() != null) {
            response.setChildrenIds(parent.getChildren().stream()
                    .map(Student::getId)
                    .collect(Collectors.toSet()));
            response.setTotalChildren(parent.getChildren().size());
        } else {
            response.setTotalChildren(0);
        }

        return response;
    }

    public void updateEntityFromRequest(UpdateParentRequest request, Parent parent) {
        if (request == null || parent == null) return;

        if (request.getEmail() != null) parent.setEmail(request.getEmail());
        if (request.getFirstName() != null) parent.setFirstName(request.getFirstName());
        if (request.getLastName() != null) parent.setLastName(request.getLastName());
        if (request.getFirstName() != null || request.getLastName() != null) {
            parent.setFullName(parent.getFirstName() + " " + parent.getLastName());
        }
        if (request.getDateOfBirth() != null) parent.setDateOfBirth(request.getDateOfBirth());
        if (request.getPhoneNumber() != null) parent.setPhoneNumber(request.getPhoneNumber());
        if (request.getParentPhoneNumber() != null) parent.setPhoneNumber(request.getParentPhoneNumber());
        if (request.getAddress() != null) parent.setAddress(request.getAddress());
        if (request.getCity() != null) parent.setCity(request.getCity());
        if (request.getState() != null) parent.setState(request.getState());
        if (request.getCountry() != null) parent.setCountry(request.getCountry());
        if (request.getPhotoUrl() != null) parent.setPhotoUrl(request.getPhotoUrl());
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
