package com.badminton.academy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "coaches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "batches")
@ToString(callSuper = true, exclude = "batches")
@PrimaryKeyJoinColumn(name = "user_id")
public class Coach extends User {

    private String specialization;

    private Integer yearsOfExperience;

    @Column(length = 1000)
    private String bio;

    private String certifications;

    @OneToMany(mappedBy = "coach", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Batch> batches = new HashSet<>();
}