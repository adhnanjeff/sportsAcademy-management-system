package com.badminton.academy.service;

import com.badminton.academy.dto.request.BulkAttendanceRequest;
import com.badminton.academy.dto.request.MarkAttendanceRequest;
import com.badminton.academy.dto.request.StudentAttendanceItem;
import com.badminton.academy.dto.response.AttendanceCellResponse;
import com.badminton.academy.dto.response.AttendanceDateColumnResponse;
import com.badminton.academy.dto.response.BatchAttendanceMatrixResponse;
import com.badminton.academy.dto.response.AttendanceResponse;
import com.badminton.academy.dto.response.AttendanceSummaryResponse;
import com.badminton.academy.dto.response.StudentAttendanceMatrixRowResponse;
import com.badminton.academy.dto.response.AttendanceAuditLogResponse;
import com.badminton.academy.exception.DuplicateResourceException;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Attendance;
import com.badminton.academy.model.AttendanceAuditLog;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.Student;
import com.badminton.academy.model.enums.AttendanceStatus;
import com.badminton.academy.model.enums.AttendanceEntryType;
import com.badminton.academy.repository.AttendanceRepository;
import com.badminton.academy.repository.AttendanceAuditLogRepository;
import com.badminton.academy.repository.BatchRepository;
import com.badminton.academy.repository.CoachRepository;
import com.badminton.academy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceAuditLogRepository auditLogRepository;
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final CoachRepository coachRepository;

    /**
     * Number of days coaches can backdate attendance (default: 7 days)
     */
    @Value("${attendance.backdate.coach-window-days:7}")
    private int coachBackdateWindowDays;

    /**
     * Number of days admins can backdate attendance (default: 30 days)
     */
    @Value("${attendance.backdate.admin-window-days:30}")
    private int adminBackdateWindowDays;

    public List<AttendanceResponse> getAllAttendances() {
        return attendanceRepository.findAll().stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    public AttendanceResponse getAttendanceById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));
        return mapToAttendanceResponse(attendance);
    }

    public List<AttendanceResponse> getAttendanceByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAttendanceByBatch(Long batchId) {
        return attendanceRepository.findByBatchId(batchId).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAttendanceByStudentAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByStudentAndDateRange(studentId, startDate, endDate).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAttendanceByBatchAndDateRange(Long batchId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByBatchAndDateRange(batchId, startDate, endDate).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAttendanceByCoachAndDate(Long coachId, LocalDate date) {
        return attendanceRepository.findByCoachAndDate(coachId, date).stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendanceResponse markAttendance(MarkAttendanceRequest request, Long coachId, boolean isAdmin) {
        boolean isBackdated = isBackdatedDate(request.getDate());
        
        // Validate backdating permissions and reason
        validateBackdatePermission(request.getDate(), isAdmin, request.getBackdateReason());
        
        // Validate makeup attendance
        validateMakeupAttendance(request);

        // Check if attendance already exists for this student, batch, and date
        if (attendanceRepository.existsByStudentIdAndBatchIdAndDate(
                request.getStudentId(), request.getBatchId(), request.getDate())) {
            throw new DuplicateResourceException("Attendance already marked for this student on this date in this batch");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + request.getBatchId()));

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        AttendanceEntryType entryType = request.getEntryType() != null 
                ? request.getEntryType() 
                : AttendanceEntryType.REGULAR;

        Attendance attendance = Attendance.builder()
                .student(student)
                .batch(batch)
                .date(request.getDate())
                .status(request.getStatus())
                .entryType(entryType)
                .compensatesForDate(request.getCompensatesForDate())
                .notes(request.getNotes())
                .markedBy(coach)
                .wasBackdated(isBackdated)
                .backdateReason(isBackdated ? request.getBackdateReason() : null)
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        
        // Create audit log
        createAuditLog(savedAttendance, "CREATE", null, null, null, 
                coach, isAdmin ? "ADMIN" : "COACH", request.getBackdateReason(), isBackdated);

        log.info("Attendance marked for student {} in batch {} on {} (type: {}, backdated: {})", 
                request.getStudentId(), request.getBatchId(), request.getDate(), entryType, isBackdated);
        return mapToAttendanceResponse(savedAttendance);
    }

    /**
     * Legacy method for backward compatibility - defaults to non-admin
     */
    @Transactional
    public AttendanceResponse markAttendance(MarkAttendanceRequest request, Long coachId) {
        return markAttendance(request, coachId, false);
    }

    @Transactional
    public List<AttendanceResponse> markBulkAttendance(BulkAttendanceRequest request, Long coachId, boolean isAdmin) {
        boolean isBackdated = isBackdatedDate(request.getDate());
        
        // Validate backdating permissions and reason
        validateBackdatePermission(request.getDate(), isAdmin, request.getBackdateReason());

        Batch batch = batchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + request.getBatchId()));

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        List<AttendanceResponse> responses = new ArrayList<>();

        int created = 0;
        int updated = 0;

        for (StudentAttendanceItem item : request.getStudentAttendances()) {
            Student student = studentRepository.findById(item.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + item.getStudentId()));

            // Validate makeup if applicable
            if (item.getEntryType() == AttendanceEntryType.MAKEUP) {
                validateMakeupItem(item, student.getId(), batch.getId());
            }

            Attendance attendance = attendanceRepository.findByStudentIdAndBatchIdAndDate(
                    item.getStudentId(), request.getBatchId(), request.getDate()
            ).orElse(null);

            AttendanceEntryType entryType = item.getEntryType() != null 
                    ? item.getEntryType() 
                    : AttendanceEntryType.REGULAR;

            if (attendance == null) {
                attendance = Attendance.builder()
                        .student(student)
                        .batch(batch)
                        .date(request.getDate())
                        .status(item.getStatus())
                        .entryType(entryType)
                        .compensatesForDate(item.getCompensatesForDate())
                        .notes(item.getNotes())
                        .markedBy(coach)
                        .wasBackdated(isBackdated)
                        .backdateReason(isBackdated ? request.getBackdateReason() : null)
                        .build();
                
                Attendance savedAttendance = attendanceRepository.save(attendance);
                createAuditLog(savedAttendance, "CREATE", null, null, null,
                        coach, isAdmin ? "ADMIN" : "COACH", request.getBackdateReason(), isBackdated);
                created++;
                responses.add(mapToAttendanceResponse(savedAttendance));
            } else {
                // Store previous values for audit
                AttendanceStatus prevStatus = attendance.getStatus();
                AttendanceEntryType prevEntryType = attendance.getEntryType();
                String prevNotes = attendance.getNotes();
                
                attendance.setStatus(item.getStatus());
                attendance.setEntryType(entryType);
                attendance.setCompensatesForDate(item.getCompensatesForDate());
                attendance.setNotes(item.getNotes());
                attendance.setMarkedBy(coach);
                attendance.setMarkedAt(java.time.LocalDateTime.now());
                if (isBackdated) {
                    attendance.setWasBackdated(true);
                    attendance.setBackdateReason(request.getBackdateReason());
                }

                Attendance savedAttendance = attendanceRepository.save(attendance);
                createAuditLog(savedAttendance, "UPDATE", prevStatus, prevEntryType, prevNotes,
                        coach, isAdmin ? "ADMIN" : "COACH", request.getBackdateReason(), isBackdated);
                updated++;
                responses.add(mapToAttendanceResponse(savedAttendance));
            }
        }

        log.info("Bulk attendance processed for batch {} on {} - {} created, {} updated (backdated: {})",
                request.getBatchId(), request.getDate(), created, updated, isBackdated);
        return responses;
    }

    /**
     * Legacy method for backward compatibility - defaults to non-admin
     */
    @Transactional
    public List<AttendanceResponse> markBulkAttendance(BulkAttendanceRequest request, Long coachId) {
        return markBulkAttendance(request, coachId, false);
    }

    @Transactional
    public AttendanceResponse updateAttendance(Long id, MarkAttendanceRequest request, Long coachId, boolean isAdmin) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));

        boolean isBackdated = isBackdatedDate(attendance.getDate());
        validateBackdatePermission(attendance.getDate(), isAdmin, request.getBackdateReason());

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        // Store previous values for audit
        AttendanceStatus prevStatus = attendance.getStatus();
        AttendanceEntryType prevEntryType = attendance.getEntryType();
        String prevNotes = attendance.getNotes();

        if (request.getStatus() != null) attendance.setStatus(request.getStatus());
        if (request.getEntryType() != null) attendance.setEntryType(request.getEntryType());
        if (request.getCompensatesForDate() != null) attendance.setCompensatesForDate(request.getCompensatesForDate());
        if (request.getNotes() != null) attendance.setNotes(request.getNotes());
        attendance.setMarkedBy(coach);
        attendance.setMarkedAt(java.time.LocalDateTime.now());
        
        if (isBackdated) {
            attendance.setWasBackdated(true);
            attendance.setBackdateReason(request.getBackdateReason());
        }

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        createAuditLog(updatedAttendance, "UPDATE", prevStatus, prevEntryType, prevNotes,
                coach, isAdmin ? "ADMIN" : "COACH", request.getBackdateReason(), isBackdated);
        
        log.info("Attendance updated for id: {} (backdated: {})", id, isBackdated);
        return mapToAttendanceResponse(updatedAttendance);
    }

    /**
     * Legacy method for backward compatibility - defaults to non-admin
     */
    @Transactional
    public AttendanceResponse updateAttendance(Long id, MarkAttendanceRequest request, Long coachId) {
        return updateAttendance(id, request, coachId, false);
    }

    @Transactional
    public void deleteAttendance(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
        log.info("Attendance deleted with id: {}", id);
    }

    public AttendanceSummaryResponse getStudentAttendanceSummary(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Long presentCount = attendanceRepository.countByStudentAndStatus(studentId, AttendanceStatus.PRESENT);
        Long absentCount = attendanceRepository.countByStudentAndStatus(studentId, AttendanceStatus.ABSENT);
        Long lateCount = attendanceRepository.countByStudentAndStatus(studentId, AttendanceStatus.LATE);
        Long excusedCount = attendanceRepository.countByStudentAndStatus(studentId, AttendanceStatus.EXCUSED);

        Long totalClasses = presentCount + absentCount + lateCount + excusedCount;
        Double attendancePercentage = totalClasses > 0 ? (presentCount * 100.0) / totalClasses : 0.0;

        return AttendanceSummaryResponse.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .totalClasses(totalClasses)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .excusedCount(excusedCount)
                .attendancePercentage(attendancePercentage)
                .build();
    }

    public Double calculateAttendancePercentage(Long studentId, Long batchId) {
        return attendanceRepository.calculateAttendancePercentage(studentId, batchId);
    }

    public BatchAttendanceMatrixResponse getBatchWeeklyAttendance(Long batchId, LocalDate referenceDate) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));

        LocalDate resolvedReferenceDate = referenceDate != null ? referenceDate : LocalDate.now();
        LocalDate startDate = resolvedReferenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate endDate = startDate.plusDays(6);

        return buildBatchAttendanceMatrix(batch, startDate, endDate, resolvedReferenceDate, "WEEKLY");
    }

    public BatchAttendanceMatrixResponse getBatchMonthlyAttendance(Long batchId, Integer year, Integer month, LocalDate referenceDate) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + batchId));

        if (year == null || month == null) {
            throw new IllegalArgumentException("Year and month are required");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        LocalDate startDate = LocalDate.of(year, Month.of(month), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate resolvedReferenceDate = referenceDate != null ? referenceDate : LocalDate.now();

        return buildBatchAttendanceMatrix(batch, startDate, endDate, resolvedReferenceDate, "MONTHLY");
    }

    private BatchAttendanceMatrixResponse buildBatchAttendanceMatrix(
            Batch batch,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate referenceDate,
            String periodType
    ) {
        LocalDate displayUntil = getDisplayUntil(startDate, endDate, referenceDate);

        List<Student> students = studentRepository.findByBatchId(batch.getId()).stream()
                .sorted(Comparator.comparing(
                        student -> student.getFullName() != null ? student.getFullName().toLowerCase() : ""
                ))
                .toList();

        Map<String, Attendance> attendanceByStudentAndDate = new HashMap<>();
        attendanceRepository.findByBatchAndDateRange(batch.getId(), startDate, endDate)
                .forEach(attendance -> attendanceByStudentAndDate.put(
                        attendance.getStudent().getId() + "_" + attendance.getDate(), attendance
                ));

        List<AttendanceDateColumnResponse> dateColumns = new ArrayList<>();
        List<LocalDate> datesInPeriod = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            datesInPeriod.add(cursor);
            dateColumns.add(AttendanceDateColumnResponse.builder()
                    .date(cursor)
                    .dayLabel(cursor.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .futureDate(cursor.isAfter(displayUntil))
                    .build());
            cursor = cursor.plusDays(1);
        }

        List<StudentAttendanceMatrixRowResponse> studentRows = students.stream()
                .map(student -> StudentAttendanceMatrixRowResponse.builder()
                        .studentId(student.getId())
                        .studentName(student.getFullName())
                        .attendance(buildAttendanceCells(student.getId(), datesInPeriod, attendanceByStudentAndDate, displayUntil))
                        .build())
                .toList();

        return BatchAttendanceMatrixResponse.builder()
                .batchId(batch.getId())
                .batchName(batch.getName())
                .periodType(periodType)
                .referenceDate(referenceDate)
                .startDate(startDate)
                .endDate(endDate)
                .displayUntil(displayUntil)
                .dateColumns(dateColumns)
                .students(studentRows)
                .build();
    }

    private List<AttendanceCellResponse> buildAttendanceCells(
            Long studentId,
            List<LocalDate> datesInPeriod,
            Map<String, Attendance> attendanceByStudentAndDate,
            LocalDate displayUntil
    ) {
        List<AttendanceCellResponse> cells = new ArrayList<>();
        for (LocalDate date : datesInPeriod) {
            boolean futureDate = date.isAfter(displayUntil);
            Attendance attendance = futureDate ? null : attendanceByStudentAndDate.get(studentId + "_" + date);

            cells.add(AttendanceCellResponse.builder()
                    .date(date)
                    .status(attendance != null ? attendance.getStatus() : null)
                    .entryType(attendance != null ? attendance.getEntryType() : null)
                    .compensatesForDate(attendance != null ? attendance.getCompensatesForDate() : null)
                    .notes(attendance != null ? attendance.getNotes() : null)
                    .marked(attendance != null)
                    .futureDate(futureDate)
                    .build());
        }
        return cells;
    }

    private LocalDate getDisplayUntil(LocalDate startDate, LocalDate endDate, LocalDate referenceDate) {
        if (referenceDate.isBefore(startDate)) {
            return startDate.minusDays(1);
        }
        if (referenceDate.isAfter(endDate)) {
            return endDate;
        }
        return referenceDate;
    }

    // ==================== AUDIT LOG METHODS ====================

    public List<AttendanceAuditLogResponse> getAuditLogByAttendanceId(Long attendanceId) {
        return auditLogRepository.findByAttendanceIdOrderByChangedAtDesc(attendanceId).stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceAuditLogResponse> getAuditLogByStudentId(Long studentId) {
        return auditLogRepository.findByStudentIdOrderByChangedAtDesc(studentId).stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceAuditLogResponse> getAuditLogByBatchId(Long batchId) {
        return auditLogRepository.findByBatchIdOrderByChangedAtDesc(batchId).stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceAuditLogResponse> getAllBackdatedChanges() {
        return auditLogRepository.findAllBackdatedChanges().stream()
                .map(this::mapToAuditLogResponse)
                .collect(Collectors.toList());
    }

    // ==================== MAKEUP ATTENDANCE METHODS ====================

    /**
     * Get eligible absences that can be compensated with a makeup session.
     * Returns absences within the allowed window that haven't been compensated yet.
     */
    public List<AttendanceResponse> getEligibleAbsencesForMakeup(Long studentId, Long batchId) {
        LocalDate today = LocalDate.now();
        LocalDate windowStart = today.minusDays(adminBackdateWindowDays); // Use admin window as max
        
        // Find absences in the window
        List<Attendance> absences = attendanceRepository.findByStudentAndDateRange(studentId, windowStart, today)
                .stream()
                .filter(a -> a.getBatch().getId().equals(batchId))
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .collect(Collectors.toList());
        
        // Filter out already compensated absences
        List<LocalDate> compensatedDates = attendanceRepository.findByStudentId(studentId).stream()
                .filter(a -> a.getEntryType() == AttendanceEntryType.MAKEUP)
                .filter(a -> a.getCompensatesForDate() != null)
                .map(Attendance::getCompensatesForDate)
                .collect(Collectors.toList());
        
        return absences.stream()
                .filter(a -> !compensatedDates.contains(a.getDate()))
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Check if a date is in the past (backdated)
     */
    private boolean isBackdatedDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Validate backdating permissions based on role and window.
     * Coaches: can backdate within coachBackdateWindowDays
     * Admins: can backdate within adminBackdateWindowDays
     */
    private void validateBackdatePermission(LocalDate date, boolean isAdmin, String reason) {
        if (date == null) {
            throw new IllegalArgumentException("Attendance date is required");
        }

        LocalDate today = LocalDate.now();
        
        // Future dates not allowed
        if (date.isAfter(today)) {
            throw new IllegalArgumentException("Cannot mark attendance for future dates");
        }

        // If it's today, no special validation needed
        if (date.equals(today)) {
            return;
        }

        // Calculate days in the past
        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(date, today);
        
        int allowedWindow = isAdmin ? adminBackdateWindowDays : coachBackdateWindowDays;
        
        if (daysAgo > allowedWindow) {
            String role = isAdmin ? "Admin" : "Coach";
            throw new IllegalArgumentException(
                    String.format("%s can only modify attendance within the last %d days. " +
                            "This date is %d days ago.", role, allowedWindow, daysAgo)
            );
        }

        // Require reason for backdated entries
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "A reason is required when marking or editing attendance for past dates"
            );
        }
    }

    /**
     * Validate makeup attendance constraints
     */
    private void validateMakeupAttendance(MarkAttendanceRequest request) {
        if (request.getEntryType() == AttendanceEntryType.MAKEUP) {
            if (request.getCompensatesForDate() == null) {
                throw new IllegalArgumentException(
                        "compensatesForDate is required for MAKEUP attendance entries"
                );
            }
            
            // Cannot compensate for future dates
            if (request.getCompensatesForDate().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "Cannot compensate for a future date"
                );
            }

            // Cannot compensate for the same day
            if (request.getCompensatesForDate().equals(request.getDate())) {
                throw new IllegalArgumentException(
                        "Makeup date cannot be the same as the compensated date"
                );
            }

            // Check if the compensated date already has a makeup
            boolean alreadyCompensated = attendanceRepository
                    .findByStudentIdAndCompensatesForDate(request.getStudentId(), request.getCompensatesForDate())
                    .isPresent();
            
            if (alreadyCompensated) {
                throw new DuplicateResourceException(
                        "This absence has already been compensated with a makeup session"
                );
            }
        }
    }

    private void validateMakeupItem(StudentAttendanceItem item, Long studentId, Long batchId) {
        if (item.getCompensatesForDate() == null) {
            throw new IllegalArgumentException(
                    "compensatesForDate is required for MAKEUP attendance entries (student: " + studentId + ")"
            );
        }
        
        if (item.getCompensatesForDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Cannot compensate for a future date (student: " + studentId + ")"
            );
        }

        // Check if already compensated
        boolean alreadyCompensated = attendanceRepository
                .findByStudentIdAndCompensatesForDate(studentId, item.getCompensatesForDate())
                .isPresent();
        
        if (alreadyCompensated) {
            throw new DuplicateResourceException(
                    "Absence on " + item.getCompensatesForDate() + " already compensated (student: " + studentId + ")"
            );
        }
    }

    // ==================== AUDIT LOG HELPER ====================

    private void createAuditLog(
            Attendance attendance,
            String action,
            AttendanceStatus prevStatus,
            AttendanceEntryType prevEntryType,
            String prevNotes,
            Coach changedBy,
            String changedByRole,
            String reason,
            boolean wasBackdated
    ) {
        AttendanceAuditLog auditLog = AttendanceAuditLog.builder()
                .attendance(attendance)
                .action(action)
                .previousStatus(prevStatus)
                .previousEntryType(prevEntryType)
                .previousNotes(prevNotes)
                .newStatus(attendance.getStatus())
                .newEntryType(attendance.getEntryType())
                .newNotes(attendance.getNotes())
                .changedBy(changedBy)
                .changedByRole(changedByRole)
                .reason(reason)
                .wasBackdated(wasBackdated)
                .build();
        
        auditLogRepository.save(auditLog);
    }

    // ==================== MAPPING METHODS ====================

    private AttendanceResponse mapToAttendanceResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getFullName())
                .batchId(attendance.getBatch().getId())
                .batchName(attendance.getBatch().getName())
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .entryType(attendance.getEntryType())
                .compensatesForDate(attendance.getCompensatesForDate())
                .notes(attendance.getNotes())
                .markedById(attendance.getMarkedBy() != null ? attendance.getMarkedBy().getId() : null)
                .markedByName(attendance.getMarkedBy() != null ? attendance.getMarkedBy().getFullName() : null)
                .markedAt(attendance.getMarkedAt())
                .wasBackdated(attendance.getWasBackdated())
                .build();
    }

    private AttendanceAuditLogResponse mapToAuditLogResponse(AttendanceAuditLog log) {
        Attendance attendance = log.getAttendance();
        return AttendanceAuditLogResponse.builder()
                .id(log.getId())
                .attendanceId(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getFullName())
                .batchId(attendance.getBatch().getId())
                .batchName(attendance.getBatch().getName())
                .attendanceDate(attendance.getDate())
                .action(log.getAction())
                .previousStatus(log.getPreviousStatus())
                .previousEntryType(log.getPreviousEntryType())
                .previousNotes(log.getPreviousNotes())
                .newStatus(log.getNewStatus())
                .newEntryType(log.getNewEntryType())
                .newNotes(log.getNewNotes())
                .changedById(log.getChangedBy() != null ? log.getChangedBy().getId() : null)
                .changedByName(log.getChangedBy() != null ? log.getChangedBy().getFullName() : null)
                .changedByRole(log.getChangedByRole())
                .reason(log.getReason())
                .wasBackdated(log.getWasBackdated())
                .changedAt(log.getChangedAt())
                .build();
    }
}
