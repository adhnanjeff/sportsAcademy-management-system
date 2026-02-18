package com.badminton.academy.service;

import com.badminton.academy.dto.request.UpdateCoachRequest;
import com.badminton.academy.dto.response.CoachResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Coach;
import com.badminton.academy.repository.CoachRepository;
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
@Transactional(readOnly = true)
public class CoachService {

    private final CoachRepository coachRepository;

    public List<CoachResponse> getAllCoaches() {
        return coachRepository.findAll().stream()
                .map(this::mapToCoachResponse)
                .collect(Collectors.toList());
    }

    public List<CoachResponse> getActiveCoaches() {
        return coachRepository.findAllActiveCoaches().stream()
                .map(this::mapToCoachResponse)
                .collect(Collectors.toList());
    }

    public CoachResponse getCoachById(Long id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));
        return mapToCoachResponse(coach);
    }

    public CoachResponse getCoachByEmail(String email) {
        Coach coach = coachRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with email: " + email));
        return mapToCoachResponse(coach);
    }

    public List<CoachResponse> getCoachesBySpecialization(String specialization) {
        return coachRepository.findBySpecialization(specialization).stream()
                .map(this::mapToCoachResponse)
                .collect(Collectors.toList());
    }

    public List<CoachResponse> getCoachesByMinimumExperience(Integer years) {
        return coachRepository.findByMinimumExperience(years).stream()
                .map(this::mapToCoachResponse)
                .collect(Collectors.toList());
    }

    public CoachResponse getCoachByBatch(Long batchId) {
        Coach coach = coachRepository.findByBatchId(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found for batch id: " + batchId));
        return mapToCoachResponse(coach);
    }

    @Transactional
    public CoachResponse updateCoach(Long id, UpdateCoachRequest request) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));

        // Update common user fields
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

        // Update coach-specific fields
        if (request.getSpecialization() != null) coach.setSpecialization(request.getSpecialization());
        if (request.getYearsOfExperience() != null) coach.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getBio() != null) coach.setBio(request.getBio());
        if (request.getCertifications() != null) coach.setCertifications(request.getCertifications());

        Coach updatedCoach = coachRepository.save(coach);
        log.info("Coach updated successfully: {}", updatedCoach.getEmail());
        return mapToCoachResponse(updatedCoach);
    }

    @Transactional
    public void deactivateCoach(Long id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));
        coach.setIsActive(false);
        coachRepository.save(coach);
        log.info("Coach deactivated: {}", coach.getEmail());
    }

    @Transactional
    public void activateCoach(Long id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));
        coach.setIsActive(true);
        coachRepository.save(coach);
        log.info("Coach activated: {}", coach.getEmail());
    }

    @Transactional
    public void deleteCoach(Long id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + id));

        if (coach.getBatches() != null && !coach.getBatches().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete coach while assigned to batches. Reassign or remove batches first.");
        }

        coachRepository.delete(coach);
        log.info("Coach deleted: {}", coach.getEmail());
    }

    public Long countActiveCoaches() {
        return coachRepository.countActiveCoaches();
    }

    private CoachResponse mapToCoachResponse(Coach coach) {
        CoachResponse response = new CoachResponse();
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

        // Coach-specific fields
        response.setSpecialization(coach.getSpecialization());
        response.setYearsOfExperience(coach.getYearsOfExperience());
        response.setBio(coach.getBio());
        response.setCertifications(coach.getCertifications());

        if (coach.getBatches() != null) {
            response.setBatchIds(coach.getBatches().stream().map(Batch::getId).collect(Collectors.toSet()));
            response.setTotalBatches(coach.getBatches().size());

            // Count total students across all batches
            int totalStudents = coach.getBatches().stream()
                    .mapToInt(batch -> batch.getStudents() != null ? batch.getStudents().size() : 0)
                    .sum();
            response.setTotalStudents(totalStudents);
        }

        return response;
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
