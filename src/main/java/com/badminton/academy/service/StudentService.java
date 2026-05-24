package com.badminton.academy.service;

import com.badminton.academy.dto.request.CreateStudentRequest;
import com.badminton.academy.dto.request.UpdateStudentRequest;
import com.badminton.academy.dto.response.FeePaymentHistoryResponse;
import com.badminton.academy.dto.response.StudentResponse;
import com.badminton.academy.exception.DuplicateResourceException;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.FeePaymentHistory;
import com.badminton.academy.model.Parent;
import com.badminton.academy.model.Student;
import com.badminton.academy.model.enums.AttendanceStatus;
import com.badminton.academy.model.enums.MonthlyFeeStatus;
import com.badminton.academy.model.enums.SkillLevel;
import com.badminton.academy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final BatchRepository batchRepository;
    private final AttendanceRepository attendanceRepository;
    private final AchievementRepository achievementRepository;
    private final SkillEvaluationRepository skillEvaluationRepository;
    private final AssessmentRepository assessmentRepository;
    private final FeePaymentHistoryRepository feePaymentHistoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "students:all")
    public List<StudentResponse> getAllStudents() {
        log.debug("Cache miss: fetching all students from database");
        return mapStudentsWithAggregatedStats(studentRepository.findAllWithDetails());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students:active")
    public List<StudentResponse> getActiveStudents() {
        log.debug("Cache miss: fetching active students from database");
        return mapStudentsWithAggregatedStats(studentRepository.findAllActiveStudents());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students:byId", key = "#id")
    public StudentResponse getStudentById(Long id) {
        log.debug("Cache miss: fetching student {} from database", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return mapToStudentResponse(student);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students:bySkillLevel", key = "#skillLevel")
    public List<StudentResponse> getStudentsBySkillLevel(SkillLevel skillLevel) {
        log.debug("Cache miss: fetching students by skill level {} from database", skillLevel);
        return mapStudentsWithAggregatedStats(studentRepository.findBySkillLevelWithDetails(skillLevel));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students:byParent", key = "#parentId")
    public List<StudentResponse> getStudentsByParent(Long parentId) {
        log.debug("Cache miss: fetching students by parent {} from database", parentId);
        return mapStudentsWithAggregatedStats(studentRepository.findByParentIdWithDetails(parentId));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students:byBatch", key = "#batchId")
    public List<StudentResponse> getStudentsByBatch(Long batchId) {
        log.debug("Cache miss: fetching students by batch {} from database", batchId);
        return mapStudentsWithAggregatedStats(studentRepository.findByBatchId(batchId));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students:byCoach", key = "#coachId")
    public List<StudentResponse> getStudentsByCoach(Long coachId) {
        log.debug("Cache miss: fetching students by coach {} from database", coachId);
        return mapStudentsWithAggregatedStats(studentRepository.findByCoachId(coachId));
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "students:all", allEntries = true),
        @CacheEvict(value = "students:active", allEntries = true),
        @CacheEvict(value = "students:bySkillLevel", allEntries = true),
        @CacheEvict(value = "students:byParent", allEntries = true),
        @CacheEvict(value = "students:byBatch", allEntries = true),
        @CacheEvict(value = "students:byCoach", allEntries = true),
        @CacheEvict(value = "students:countBySkillLevel", allEntries = true)
    })
    public StudentResponse createStudent(CreateStudentRequest request) {
        // Check for duplicate national ID if provided
        if (request.getNationalIdNumber() != null && !request.getNationalIdNumber().isBlank()) {
            if (studentRepository.existsByNationalIdNumber(request.getNationalIdNumber())) {
                throw new DuplicateResourceException("Student already exists with national ID: " + request.getNationalIdNumber());
            }
        }

        Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .fullName(request.getFirstName() + " " + request.getLastName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .nationalIdNumber(request.getNationalIdNumber())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .photoUrl(request.getPhotoUrl())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .skillLevel(request.getSkillLevel() != null ? request.getSkillLevel() : SkillLevel.BEGINNER)
                .daysOfWeek(request.getDaysOfWeek() != null ? request.getDaysOfWeek() : new HashSet<>())
                .feePayable(request.getFeePayable() != null ? request.getFeePayable() : BigDecimal.ZERO)
                .monthlyFeeStatus(request.getMonthlyFeeStatus() != null ? request.getMonthlyFeeStatus() : MonthlyFeeStatus.UNPAID)
                .isActive(true)
                .build();

        // Set parent if provided
        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + request.getParentId()));
            student.setParent(parent);
        }

        Student savedStudent = studentRepository.save(student);

        // Assign to batch if provided
        if (request.getBatchId() != null) {
            Batch batch = batchRepository.findById(request.getBatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + request.getBatchId()));
            batch.getStudents().add(savedStudent);
            batchRepository.save(batch);
            savedStudent = studentRepository.findById(savedStudent.getId()).orElse(savedStudent);
        }

        log.info("Student created successfully: {} {}", savedStudent.getFirstName(), savedStudent.getLastName());
        return mapToStudentResponse(savedStudent);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "students:all", allEntries = true),
        @CacheEvict(value = "students:active", allEntries = true),
        @CacheEvict(value = "students:byId", key = "#id"),
        @CacheEvict(value = "students:bySkillLevel", allEntries = true),
        @CacheEvict(value = "students:byParent", allEntries = true),
        @CacheEvict(value = "students:byBatch", allEntries = true),
        @CacheEvict(value = "students:byCoach", allEntries = true),
        @CacheEvict(value = "students:countBySkillLevel", allEntries = true)
    })
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Check for duplicate national ID if changing
        if (request.getNationalIdNumber() != null && !request.getNationalIdNumber().equals(student.getNationalIdNumber())) {
            if (studentRepository.existsByNationalIdNumber(request.getNationalIdNumber())) {
                throw new DuplicateResourceException("Student already exists with national ID: " + request.getNationalIdNumber());
            }
            student.setNationalIdNumber(request.getNationalIdNumber());
        }

        if (request.getFirstName() != null) student.setFirstName(request.getFirstName());
        if (request.getLastName() != null) student.setLastName(request.getLastName());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getFirstName() != null || request.getLastName() != null) {
            student.setFullName(student.getFirstName() + " " + student.getLastName());
        }
        if (request.getDateOfBirth() != null) student.setDateOfBirth(request.getDateOfBirth());
        if (request.getPhoneNumber() != null) student.setPhoneNumber(request.getPhoneNumber().trim());
        if (request.getEmail() != null) student.setEmail(request.getEmail().trim());
        if (request.getAddress() != null) student.setAddress(request.getAddress());
        if (request.getCity() != null) student.setCity(request.getCity());
        if (request.getState() != null) student.setState(request.getState());
        if (request.getCountry() != null) student.setCountry(request.getCountry());
        if (request.getPhotoUrl() != null) student.setPhotoUrl(request.getPhotoUrl());

        // Update student-specific fields
        if (request.getSkillLevel() != null) student.setSkillLevel(request.getSkillLevel());
        if (request.getDaysOfWeek() != null) student.setDaysOfWeek(request.getDaysOfWeek());
        if (request.getFeePayable() != null) student.setFeePayable(request.getFeePayable());
        if (request.getMonthlyFeeStatus() != null) student.setMonthlyFeeStatus(request.getMonthlyFeeStatus());

        // Update parent if provided
        if (request.getParentId() != null) {
            Parent parent = parentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent not found with id: " + request.getParentId()));
            student.setParent(parent);
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Student updated successfully: {} {}", updatedStudent.getFirstName(), updatedStudent.getLastName());
        return mapToStudentResponse(updatedStudent);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "students:all", allEntries = true),
        @CacheEvict(value = "students:active", allEntries = true),
        @CacheEvict(value = "students:byId", key = "#studentId"),
        @CacheEvict(value = "students:byBatch", allEntries = true),
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:byId", key = "#batchId")
    })
    public StudentResponse assignToBatch(Long studentId, Long batchId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));

        // Check if student is already in this batch to prevent duplicate key violation
        boolean alreadyInBatch = batch.getStudents().stream()
                .anyMatch(s -> s.getId().equals(studentId));
        
        if (alreadyInBatch) {
            log.info("Student {} is already assigned to batch {}, skipping", studentId, batchId);
            return mapToStudentResponse(student);
        }

        // Add bidirectional relationship
        student.getBatches().add(batch);
        batch.getStudents().add(student);

        batchRepository.save(batch);
        Student updatedStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        log.info("Student {} assigned to batch {}", studentId, batchId);
        return mapToStudentResponse(updatedStudent);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "students:all", allEntries = true),
        @CacheEvict(value = "students:active", allEntries = true),
        @CacheEvict(value = "students:byId", key = "#studentId"),
        @CacheEvict(value = "students:byBatch", allEntries = true),
        @CacheEvict(value = "batches:all", allEntries = true),
        @CacheEvict(value = "batches:byId", key = "#batchId")
    })
    public StudentResponse removeFromBatch(Long studentId, Long batchId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));

        student.getBatches().remove(batch);
        batch.getStudents().remove(student);

        batchRepository.save(batch);
        Student updatedStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        log.info("Student {} removed from batch {}", studentId, batchId);
        return mapToStudentResponse(updatedStudent);
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "students:all", allEntries = true),
        @CacheEvict(value = "students:active", allEntries = true),
        @CacheEvict(value = "students:byId", key = "#id"),
        @CacheEvict(value = "students:bySkillLevel", allEntries = true),
        @CacheEvict(value = "students:countBySkillLevel", allEntries = true)
    })
    public void deactivateStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        student.setIsActive(false);
        studentRepository.save(student);
        log.info("Student deactivated: {} {}", student.getFirstName(), student.getLastName());
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "students:all", allEntries = true),
        @CacheEvict(value = "students:active", allEntries = true),
        @CacheEvict(value = "students:byId", key = "#id"),
        @CacheEvict(value = "students:bySkillLevel", allEntries = true),
        @CacheEvict(value = "students:byParent", allEntries = true),
        @CacheEvict(value = "students:byBatch", allEntries = true),
        @CacheEvict(value = "students:byCoach", allEntries = true),
        @CacheEvict(value = "students:countBySkillLevel", allEntries = true),
        @CacheEvict(value = "students:feeHistory", key = "#id"),
        @CacheEvict(value = "batches:all", allEntries = true)
    })
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Delete from batch_students join table first using native query
        // This is the most reliable way to handle the many-to-many relationship
        batchRepository.removeStudentFromAllBatches(id);

        // Explicitly delete dependent student records before deleting the student.
        // This provides predictable behavior even when DB-level cascade differs by environment.
        attendanceRepository.deleteByStudentId(id);
        achievementRepository.deleteByStudentId(id);
        skillEvaluationRepository.deleteByStudentId(id);
        assessmentRepository.deleteByStudentId(id);
        feePaymentHistoryRepository.deleteByStudentId(id);

        studentRepository.delete(student);
        
        log.info("Student deleted: {} {}", student.getFirstName(), student.getLastName());
    }

    @Cacheable(value = "students:countBySkillLevel", key = "#skillLevel")
    public Long countBySkillLevel(SkillLevel skillLevel) {
        log.debug("Cache miss: counting students by skill level {} from database", skillLevel);
        return studentRepository.countBySkillLevel(skillLevel);
    }

    private StudentResponse mapToStudentResponse(Student student) {
        return mapToStudentResponse(student, null);
    }

    private StudentResponse mapToStudentResponse(Student student, AggregatedStudentStats aggregatedStats) {
        StudentResponse response = StudentResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getFullName())
                .gender(student.getGender())
                .nationalIdNumber(student.getNationalIdNumber())
                .dateOfBirth(student.getDateOfBirth())
                .age(calculateAge(student.getDateOfBirth()))
                .photoUrl(student.getPhotoUrl())
                .phoneNumber(student.getPhoneNumber())
                .email(student.getEmail())
                .address(student.getAddress())
                .city(student.getCity())
                .state(student.getState())
                .country(student.getCountry())
                .isActive(student.getIsActive())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .skillLevel(student.getSkillLevel())
                .daysOfWeek(student.getDaysOfWeek() != null ? new HashSet<>(student.getDaysOfWeek()) : null)
                .feePayable(student.getFeePayable())
                .monthlyFeeStatus(student.getMonthlyFeeStatus())
                .build();

        if (student.getParent() != null) {
            response.setParentId(student.getParent().getId());
            response.setParentName(student.getParent().getFullName());
        }

        if (student.getBatches() != null) {
            response.setBatchIds(student.getBatches().stream().map(Batch::getId).collect(Collectors.toSet()));
            response.setBatchNames(student.getBatches().stream().map(Batch::getName).collect(Collectors.toSet()));
            response.setTotalBatches(student.getBatches().size());
        }

        // Calculate statistics
        if (aggregatedStats != null) {
            long verifiedAchievements = aggregatedStats.verifiedAchievementsByStudentId.getOrDefault(student.getId(), 0L);
            response.setTotalAchievements((int) verifiedAchievements);

            AttendanceCounts attendanceCounts = aggregatedStats.attendanceCountsByStudentId.get(student.getId());
            long presentCount = attendanceCounts != null ? attendanceCounts.present : 0L;
            long absentCount = attendanceCounts != null ? attendanceCounts.absent : 0L;
            long lateCount = attendanceCounts != null ? attendanceCounts.late : 0L;
            long totalAttendance = presentCount + absentCount + lateCount;
            if (totalAttendance > 0L) {
                response.setAttendancePercentage((presentCount * 100.0) / totalAttendance);
            } else {
                response.setAttendancePercentage(0.0);
            }
            response.setAverageSkillRating(aggregatedStats.averageSkillRatingByStudentId.getOrDefault(student.getId(), 0.0));
        } else {
            try {
                Long verifiedAchievements = achievementRepository.countVerifiedAchievementsByStudentId(student.getId());
                response.setTotalAchievements(verifiedAchievements != null ? verifiedAchievements.intValue() : 0);
            } catch (Exception ex) {
                log.warn("Failed to calculate achievements for student {}: {}", student.getId(), ex.getMessage());
                response.setTotalAchievements(0);
            }

            try {
                Long presentCount = defaultLong(attendanceRepository.countByStudentAndStatus(student.getId(), AttendanceStatus.PRESENT));
                Long absentCount = defaultLong(attendanceRepository.countByStudentAndStatus(student.getId(), AttendanceStatus.ABSENT));
                Long lateCount = defaultLong(attendanceRepository.countByStudentAndStatus(student.getId(), AttendanceStatus.LATE));
                Long totalAttendance = presentCount + absentCount + lateCount;
                if (totalAttendance > 0) {
                    response.setAttendancePercentage((presentCount * 100.0) / totalAttendance);
                } else {
                    response.setAttendancePercentage(0.0);
                }
            } catch (Exception ex) {
                log.warn("Failed to calculate attendance for student {}: {}", student.getId(), ex.getMessage());
                response.setAttendancePercentage(0.0);
            }

            try {
                Double averageSkillRating = skillEvaluationRepository.getAverageOverallScoreByStudentId(student.getId());
                response.setAverageSkillRating(averageSkillRating != null ? averageSkillRating : 0.0);
            } catch (Exception ex) {
                log.warn("Failed to calculate skill rating for student {}: {}", student.getId(), ex.getMessage());
                response.setAverageSkillRating(0.0);
            }
        }

        return response;
    }

    private StudentResponse safeMapToStudentResponse(Student student) {
        return safeMapToStudentResponse(student, null);
    }

    private StudentResponse safeMapToStudentResponse(Student student, AggregatedStudentStats aggregatedStats) {
        try {
            return mapToStudentResponse(student, aggregatedStats);
        } catch (Exception ex) {
            log.error("Failed to map student {} to response: {}", student != null ? student.getId() : null, ex.getMessage(), ex);
            return null;
        }
    }

    private List<StudentResponse> mapStudentsWithAggregatedStats(List<Student> students) {
        AggregatedStudentStats aggregatedStats = loadAggregatedStats(students);
        return students.stream()
                .map(student -> safeMapToStudentResponse(student, aggregatedStats))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AggregatedStudentStats loadAggregatedStats(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return AggregatedStudentStats.empty();
        }

        List<Long> studentIds = students.stream()
                .map(Student::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return AggregatedStudentStats.empty();
        }

        Map<Long, Long> verifiedAchievementsByStudentId = new HashMap<>();
        Map<Long, AttendanceCounts> attendanceCountsByStudentId = new HashMap<>();
        Map<Long, Double> averageSkillRatingByStudentId = new HashMap<>();

        try {
            List<Object[]> verifiedAchievementRows = achievementRepository.countVerifiedAchievementsByStudentIds(studentIds);
            for (Object[] row : verifiedAchievementRows) {
                Long studentId = asLong(row[0]);
                Long achievementCount = asLong(row[1]);
                if (studentId != null) {
                    verifiedAchievementsByStudentId.put(studentId, achievementCount != null ? achievementCount : 0L);
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to bulk fetch achievement counts for students: {}", ex.getMessage());
        }

        try {
            List<Object[]> attendanceRows = attendanceRepository.aggregateAttendanceCountsByStudentIds(
                    studentIds,
                    AttendanceStatus.PRESENT,
                    AttendanceStatus.ABSENT,
                    AttendanceStatus.LATE
            );
            for (Object[] row : attendanceRows) {
                Long studentId = asLong(row[0]);
                Long presentCount = asLong(row[1]);
                Long absentCount = asLong(row[2]);
                Long lateCount = asLong(row[3]);
                if (studentId != null) {
                    attendanceCountsByStudentId.put(
                            studentId,
                            new AttendanceCounts(
                                    presentCount != null ? presentCount : 0L,
                                    absentCount != null ? absentCount : 0L,
                                    lateCount != null ? lateCount : 0L
                            )
                    );
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to bulk fetch attendance counts for students: {}", ex.getMessage());
        }

        try {
            List<Object[]> averageSkillRows = skillEvaluationRepository.getAverageOverallScoreByStudentIds(studentIds);
            for (Object[] row : averageSkillRows) {
                Long studentId = asLong(row[0]);
                Double averageSkill = asDouble(row[1]);
                if (studentId != null) {
                    averageSkillRatingByStudentId.put(studentId, averageSkill != null ? averageSkill : 0.0);
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to bulk fetch average skill ratings for students: {}", ex.getMessage());
        }

        return new AggregatedStudentStats(
                verifiedAchievementsByStudentId,
                attendanceCountsByStudentId,
                averageSkillRatingByStudentId
        );
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return null;
    }

    private static final class AggregatedStudentStats {
        private final Map<Long, Long> verifiedAchievementsByStudentId;
        private final Map<Long, AttendanceCounts> attendanceCountsByStudentId;
        private final Map<Long, Double> averageSkillRatingByStudentId;

        private AggregatedStudentStats(
                Map<Long, Long> verifiedAchievementsByStudentId,
                Map<Long, AttendanceCounts> attendanceCountsByStudentId,
                Map<Long, Double> averageSkillRatingByStudentId
        ) {
            this.verifiedAchievementsByStudentId = verifiedAchievementsByStudentId;
            this.attendanceCountsByStudentId = attendanceCountsByStudentId;
            this.averageSkillRatingByStudentId = averageSkillRatingByStudentId;
        }

        private static AggregatedStudentStats empty() {
            return new AggregatedStudentStats(
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            );
        }
    }

    private static final class AttendanceCounts {
        private final long present;
        private final long absent;
        private final long late;

        private AttendanceCounts(long present, long absent, long late) {
            this.present = present;
            this.absent = absent;
            this.late = late;
        }
    }

    private Long defaultLong(Long value) {
        return value != null ? value : 0L;
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students:feeHistory", key = "#studentId")
    public List<FeePaymentHistoryResponse> getFeePaymentHistory(Long studentId) {
        log.debug("Cache miss: fetching fee payment history for student {} from database", studentId);
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        return feePaymentHistoryRepository.findByStudentIdOrderByYearDescMonthDesc(studentId).stream()
                .map(this::mapToFeePaymentHistoryResponse)
                .collect(Collectors.toList());
    }

    private FeePaymentHistoryResponse mapToFeePaymentHistoryResponse(FeePaymentHistory history) {
        return FeePaymentHistoryResponse.builder()
                .id(history.getId())
                .month(history.getMonthName() + " " + history.getYear())
                .year(history.getYear())
                .monthNumber(history.getMonth())
                .status(history.getStatus())
                .amountPaid(history.getAmountPaid())
                .feePayable(history.getFeePayable())
                .paidDate(history.getPaidDate())
                .build();
    }
}
