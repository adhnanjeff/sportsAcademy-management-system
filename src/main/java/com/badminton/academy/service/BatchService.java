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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @Cacheable(value = "batches:all")
    public List<BatchResponse> getAllBatches() {
        log.debug("Cache miss: fetching all batches from database");
        return batchRepository.findAll().stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "batches:active")
    public List<BatchResponse> getActiveBatches() {
        log.debug("Cache miss: fetching active batches from database");
        return batchRepository.findByIsActiveTrue().stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "batches:byId", key = "#id")
    public BatchResponse getBatchById(Long id) {
        log.debug("Cache miss: fetching batch {} from database", id);
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
        return mapToBatchResponse(batch);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "batches:byCoach", key = "#coachId")
    public List<BatchResponse> getBatchesByCoach(Long coachId) {
        log.debug("Cache miss: fetching batches by coach {} from database", coachId);
        return batchRepository.findByCoachId(coachId).stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "batches:byCoach", key = "'active:' + #coachId")
    public List<BatchResponse> getActiveBatchesByCoach(Long coachId) {
        log.debug("Cache miss: fetching active batches by coach {} from database", coachId);
        return batchRepository.findActiveByCoachId(coachId).stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "batches:bySkillLevel", key = "#skillLevel")
    public List<BatchResponse> getBatchesBySkillLevel(SkillLevel skillLevel) {
        log.debug("Cache miss: fetching batches by skill level {} from database", skillLevel);
        return batchRepository.findBySkillLevel(skillLevel).stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "batches:byStudent", key = "#studentId")
    public List<BatchResponse> getBatchesByStudent(Long studentId) {
        log.debug("Cache miss: fetching batches by student {} from database", studentId);
        return batchRepository.findByStudentId(studentId).stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "batches:withSlots")
    public List<BatchResponse> getBatchesWithAvailableSlots() {
        log.debug("Cache miss: fetching batches with available slots from database");
        return batchRepository.findBatchesWithAvailableSlots().stream()
                .map(this::mapToBatchResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:active", allEntries = true),
        @CacheEvict(value = "batches:byCoach", allEntries = true),
        @CacheEvict(value = "batches:bySkillLevel", allEntries = true),
        @CacheEvict(value = "batches:withSlots", allEntries = true)
    })
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
    @Caching(evict = {
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:active", allEntries = true),
        @CacheEvict(value = "batches:byId", key = "#id"),
        @CacheEvict(value = "batches:byCoach", allEntries = true),
        @CacheEvict(value = "batches:bySkillLevel", allEntries = true),
        @CacheEvict(value = "batches:withSlots", allEntries = true)
    })
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
    @Caching(evict = {
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:byId", key = "#batchId"),
        @CacheEvict(value = "batches:byStudent", allEntries = true),
        @CacheEvict(value = "batches:withSlots", allEntries = true),
        @CacheEvict(value = "students:all", allEntries = true),
        @CacheEvict(value = "students:byId", key = "#studentId"),
        @CacheEvict(value = "students:byBatch", allEntries = true)
    })
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
    @Caching(evict = {
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:byId", key = "#batchId"),
        @CacheEvict(value = "batches:byStudent", allEntries = true),
        @CacheEvict(value = "batches:withSlots", allEntries = true),
        @CacheEvict(value = "students:all", allEntries = true),
        @CacheEvict(value = "students:byId", key = "#studentId"),
        @CacheEvict(value = "students:byBatch", allEntries = true)
    })
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
    @Caching(evict = {
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:active", allEntries = true),
        @CacheEvict(value = "batches:byId", key = "#id"),
        @CacheEvict(value = "batches:withSlots", allEntries = true)
    })
    public void deactivateBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
        batch.setIsActive(false);
        batchRepository.save(batch);
        log.info("Batch deactivated: {}", batch.getName());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:active", allEntries = true),
        @CacheEvict(value = "batches:byId", key = "#id"),
        @CacheEvict(value = "batches:withSlots", allEntries = true)
    })
    public void activateBatch(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
        batch.setIsActive(true);
        batchRepository.save(batch);
        log.info("Batch activated: {}", batch.getName());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:active", allEntries = true),
        @CacheEvict(value = "batches:byId", key = "#id"),
        @CacheEvict(value = "batches:byCoach", allEntries = true),
        @CacheEvict(value = "batches:bySkillLevel", allEntries = true),
        @CacheEvict(value = "batches:byStudent", allEntries = true),
        @CacheEvict(value = "batches:withSlots", allEntries = true)
    })
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
