package com.badminton.academy.model;

import jakarta.persistence.*;
import lombok.*;

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
public class Parent extends User {

    // Note: phoneNumber is inherited from User entity
    // Parent can use the same phoneNumber field from User

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Student> children = new HashSet<>();
}