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
import com.badminton.academy.exception.DuplicateResourceException;
import com.badminton.academy.exception.ResourceNotFoundException;
import com.badminton.academy.model.Attendance;
import com.badminton.academy.model.Batch;
import com.badminton.academy.model.Coach;
import com.badminton.academy.model.Student;
import com.badminton.academy.model.enums.AttendanceStatus;
import com.badminton.academy.repository.AttendanceRepository;
import com.badminton.academy.repository.BatchRepository;
import com.badminton.academy.repository.CoachRepository;
import com.badminton.academy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final CoachRepository coachRepository;

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
    public AttendanceResponse markAttendance(MarkAttendanceRequest request, Long coachId) {
        validateAttendanceDateForModification(request.getDate());

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

        Attendance attendance = Attendance.builder()
                .student(student)
                .batch(batch)
                .date(request.getDate())
                .status(request.getStatus())
                .notes(request.getNotes())
                .markedBy(coach)
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Attendance marked for student {} in batch {} on {}", 
                request.getStudentId(), request.getBatchId(), request.getDate());
        return mapToAttendanceResponse(savedAttendance);
    }

    @Transactional
    public List<AttendanceResponse> markBulkAttendance(BulkAttendanceRequest request, Long coachId) {
        validateAttendanceDateForModification(request.getDate());

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

            Attendance attendance = attendanceRepository.findByStudentIdAndBatchIdAndDate(
                    item.getStudentId(), request.getBatchId(), request.getDate()
            ).orElse(null);

            if (attendance == null) {
                attendance = Attendance.builder()
                        .student(student)
                        .batch(batch)
                        .date(request.getDate())
                        .status(item.getStatus())
                        .notes(item.getNotes())
                        .markedBy(coach)
                        .build();
                created++;
            } else {
                attendance.setStatus(item.getStatus());
                attendance.setNotes(item.getNotes());
                attendance.setMarkedBy(coach);
                attendance.setMarkedAt(java.time.LocalDateTime.now());
                updated++;
            }

            Attendance savedAttendance = attendanceRepository.save(attendance);
            responses.add(mapToAttendanceResponse(savedAttendance));
        }

        log.info("Bulk attendance processed for batch {} on {} - {} created, {} updated",
                request.getBatchId(), request.getDate(), created, updated);
        return responses;
    }

    @Transactional
    public AttendanceResponse updateAttendance(Long id, MarkAttendanceRequest request, Long coachId) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));

        validateAttendanceDateForModification(attendance.getDate());

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found with id: " + coachId));

        if (request.getStatus() != null) attendance.setStatus(request.getStatus());
        if (request.getNotes() != null) attendance.setNotes(request.getNotes());
        attendance.setMarkedBy(coach);

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        log.info("Attendance updated for id: {}", id);
        return mapToAttendanceResponse(updatedAttendance);
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

    private void validateAttendanceDateForModification(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Attendance date is required");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Attendance for past dates is locked and cannot be modified");
        }
    }

    private AttendanceResponse mapToAttendanceResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getFullName())
                .batchId(attendance.getBatch().getId())
                .batchName(attendance.getBatch().getName())
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .notes(attendance.getNotes())
                .markedById(attendance.getMarkedBy() != null ? attendance.getMarkedBy().getId() : null)
                .markedByName(attendance.getMarkedBy() != null ? attendance.getMarkedBy().getFullName() : null)
                .markedAt(attendance.getMarkedAt())
                .build();
    }
}
