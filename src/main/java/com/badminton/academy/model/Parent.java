package com.badminton.academy.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "parents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "children")
@ToString(callSuper = true, exclude = "children")
@PrimaryKeyJoinColumn(name = "user_id")
@BatchSize(size = 50)
public class Parent extends User {

    // Note: phoneNumber is inherited from User entity
    // Parent can use the same phoneNumber field from User

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private Set<Student> children = new HashSet<>();
}