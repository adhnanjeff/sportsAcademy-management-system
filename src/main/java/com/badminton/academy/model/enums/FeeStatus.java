package com.badminton.academy.model.enums;

public enum FeeStatus {
    PAID,           // Fee is paid for current period
    PENDING,        // Fee is pending but not overdue
    OVERDUE,        // Fee payment is overdue
    PARTIAL,        // Partial payment made
    EXEMPTED        // Student is exempted from fees (scholarship, etc.)
}