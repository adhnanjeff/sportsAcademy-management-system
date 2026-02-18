package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateBatchRequest;
import com.badminton.academy.dto.request.UpdateBatchRequest;
import com.badminton.academy.dto.response.BatchResponse;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.Student;
import com.badminton.academy.model.enums.SkillLevel;
import com.badminton.academy.repository.BatchRepository;
import com.badminton.academy.repository.CoachRepository;
import com.badminton.academy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {

    private final BatchRepository batchRepository;
    private final CoachRepository coachRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<BatchResponse> getAllBatches() {
        return batchRepository.findAll().stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getActiveBatches() {
        return batchRepository.findByIsActiveTrue().stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BatchResponse getBatchById(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
        return mapToBatchResponse(batch);
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByCoach(Long coachId) {
        return batchRepository.findByCoachId(coachId).stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getActiveBatchesByCoach(Long coachId) {
        return batchRepository.findActiveByCoachId(coachId).stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesBySkillLevel(SkillLevel skillLevel) {
        return batchRepository.findBySkillLevel(skillLevel).stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByStudent(Long studentId) {
        return batchRepository.findByStudentId(studentId).stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesWithAvailableSlots() {
        return batchRepository.findBatchesWithAvailableSlots().stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BatchResponse createBatch(CreateBatchRequest request) {
        Coach coach = coachRepository.findById(request.getCoachId())
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + request.getCoachId()));

        Batch batch = Batch.builder()
                .name(request.getName())
                .skillLevel(request.getSkillLevel())
                .coach(coach)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(true)
                .students(new HashSet<>())
                .build();

        Batch savedBatch = batchRepository.save(batch);
        log.info("Batch created successfully: {}", savedBatch.getName());
        return mapToBatchResponse(savedBatch);
    }

    @Transactional
    public BatchResponse updateBatch(Long id, UpdateBatchRequest request) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));

        if (request.getName() != null) batch.setName(request.getName());
        if (request.getSkillLevel() != null) batch.setSkillLevel(request.getSkillLevel());
        if (request.getStartTime() != null) batch.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) batch.setEndTime(request.getEndTime());
        if (request.getIsActive() != null) batch.setIsActive(request.getIsActive());

        if (request.getCoachId() != null) {
            Coach coach = coachRepository.findById(request.getCoachId())
                    .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + request.getCoachId()));
            batch.setCoach(coach);
        }

        Batch updatedBatch = batchRepository.save(batch);
        log.info("Batch updated successfully: {}", updatedBatch.getName());
        return mapToBatchResponse(updatedBatch);
    }

    @Transactional
    public BatchResponse addStudentToBatch(Long batchId, Long studentId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        batch.getStudents().add(student);
        student.getBatches().add(batch);

        Batch updatedBatch = batchRepository.save(batch);
        log.info("Student {} added to batch {}", studentId, batchId);
        return mapToBatchResponse(updatedBatch);
    }

    @Transactional
    public BatchResponse removeStudentFromBatch(Long batchId, Long studentId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        batch.getStudents().remove(student);
        student.getBatches().remove(batch);

        Batch updatedBatch = batchRepository.save(batch);
        log.info("Student {} removed from batch {}", studentId, batchId);
        return mapToBatchResponse(updatedBatch);
    }

    @Transactional
    public void deactivateBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
        batch.setIsActive(false);
        batchRepository.save(batch);
        log.info("Batch deactivated: {}", batch.getName());
    }

    @Transactional
    public void activateBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
        batch.setIsActive(true);
        batchRepository.save(batch);
        log.info("Batch activated: {}", batch.getName());
    }

    @Transactional
    public void deleteBatch(Long id) {
        if (!batchRepository.existsById(id)) {
            throw new ResourceNotFoundException("Batch not found with id: " + id);
        }
        batchRepository.deleteById(id);
        log.info("Batch deleted with id: {}", id);
    }

    public Long countStudentsInBatch(Long batchId) {
        return batchRepository.countStudentsByBatchId(batchId);
    }

    private BatchResponse mapToBatchResponse(Batch batch) {
        return BatchResponse.builder()
                .id(batch.getId())
                .name(batch.getName())
                .skillLevel(batch.getSkillLevel())
                .coachId(batch.getCoach() != null ? batch.getCoach().getId() : null)
                .coachName(batch.getCoach() != null ? batch.getCoach().getFullName() : null)
                .startTime(batch.getStartTime())
                .endTime(batch.getEndTime())
                .isActive(batch.getIsActive())
                .totalStudents(batch.getStudents() != null ? batch.getStudents().size() : 0)
                .studentIds(batch.getStudents() != null ?
                        batch.getStudents().stream().map(Student::getId).collect(Collectors.toSet()) : null)
                .build();
    }
}
