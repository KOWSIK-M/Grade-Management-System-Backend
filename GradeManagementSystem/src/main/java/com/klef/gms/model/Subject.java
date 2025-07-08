package com.klef.gms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private SubjectType type; // ENUM: PROGRAMMING, LANGUAGE, etc.

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}

