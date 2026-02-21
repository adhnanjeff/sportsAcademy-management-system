package com.badminton.academy.dto.response;

import com.badminton.academy.model.enums.AttendanceStatus;
import com.badminton.academy.model.enums.AttendanceEntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long batchId;
    private String batchName;
    private LocalDate date;
    private AttendanceStatus status;
    
    /**
     * REGULAR or MAKEUP - indicates if this is a scheduled or makeup session
     */
    private AttendanceEntryType entryType;
    
    /**
     * For MAKEUP entries: the original missed date being compensated
     */
    private LocalDate compensatesForDate;
    
    private String notes;
    private Long markedById;
    private String markedByName;
    private LocalDateTime markedAt;
    
    /**
     * True if this record was created/modified for a past date
     */
    private Boolean wasBackdated;
}
