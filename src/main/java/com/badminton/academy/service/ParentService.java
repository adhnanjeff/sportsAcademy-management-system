package com.badminton.academy.service;

import com.badminton.academy.dto.request.UpdateParentRequest;
import com.badminton.academy.dto.response.ParentResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Parent;
import com.badminton.academy.model.Student;
import com.badminton.academy.repository.ParentRepository;
import com.badminton.academy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentService {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;

    public List<ParentResponse> getAllParents() {
        return parentRepository.findAll().stream()
                .map(this::mapToParentResponse)
                .collect(Collectors.toList());
    }

    public List<ParentResponse> getActiveParents() {
        return parentRepository.findAllActiveParents().stream()
                .map(this::mapToParentResponse)
                .collect(Collectors.toList());
    }

    public ParentResponse getParentById(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));
        return mapToParentResponse(parent);
    }

    public ParentResponse getParentByEmail(String email) {
        Parent parent = parentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with email: " + email));
        return mapToParentResponse(parent);
    }

    public ParentResponse getParentByStudent(Long studentId) {
        Parent parent = parentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found for student id: " + studentId));
        return mapToParentResponse(parent);
    }

    @Transactional
    public ParentResponse updateParent(Long id, UpdateParentRequest request) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));

        // Update common user fields
        if (request.getEmail() != null) parent.setEmail(request.getEmail());
        if (request.getFirstName() != null) parent.setFirstName(request.getFirstName());
        if (request.getLastName() != null) parent.setLastName(request.getLastName());
        if (request.getFirstName() != null || request.getLastName() != null) {
            parent.setFullName(parent.getFirstName() + " " + parent.getLastName());
        }
        if (request.getDateOfBirth() != null) parent.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null) parent.setAddress(request.getAddress());
        if (request.getCity() != null) parent.setCity(request.getCity());
        if (request.getState() != null) parent.setState(request.getState());
        if (request.getCountry() != null) parent.setCountry(request.getCountry());
        if (request.getPhotoUrl() != null) parent.setPhotoUrl(request.getPhotoUrl());

        // Update phone number (from inherited User field)
        if (request.getPhoneNumber() != null) parent.setPhoneNumber(request.getPhoneNumber());
        if (request.getParentPhoneNumber() != null) parent.setPhoneNumber(request.getParentPhoneNumber());

        Parent updatedParent = parentRepository.save(parent);
        log.info("Parent updated successfully: {}", updatedParent.getEmail());
        return mapToParentResponse(updatedParent);
    }

    @Transactional
    public ParentResponse addChild(Long parentId, Long studentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + parentId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        student.setParent(parent);
        parent.getChildren().add(student);

        Parent updatedParent = parentRepository.save(parent);
        log.info("Child {} added to parent {}", studentId, parentId);
        return mapToParentResponse(updatedParent);
    }

    @Transactional
    public ParentResponse removeChild(Long parentId, Long studentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + parentId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        student.setParent(null);
        parent.getChildren().remove(student);

        studentRepository.save(student);
        Parent updatedParent = parentRepository.save(parent);
        log.info("Child {} removed from parent {}", studentId, parentId);
        return mapToParentResponse(updatedParent);
    }

    @Transactional
    public void deactivateParent(Long id) {
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + id));
        parent.setIsActive(false);
        parentRepository.save(parent);
        log.info("Parent deactivated: {}", parent.getEmail());
    }

    public Long countChildren(Long parentId) {
        return parentRepository.countChildrenByParentId(parentId);
    }

    private ParentResponse mapToParentResponse(Parent parent) {
        ParentResponse response = new ParentResponse();
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

        // Parent-specific fields
        response.setParentPhoneNumber(parent.getPhoneNumber());

        if (parent.getChildren() != null) {
            response.setChildrenIds(parent.getChildren().stream().map(Student::getId).collect(Collectors.toSet()));
            response.setTotalChildren(parent.getChildren().size());
        }

        return response;
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
