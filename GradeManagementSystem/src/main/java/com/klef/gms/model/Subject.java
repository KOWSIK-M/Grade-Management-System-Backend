package com.klef.gms.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

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

    @NotBlank(message = "Subject name must not be blank")
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Subject type must not be null")
    private SubjectType type;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    @NotNull(message = "Department must not be null")
    private Department department;

    /**
     * Example method to demonstrate exception handling and clear logic.
     * Checks if the subject is valid.
     */
    public boolean isValid() {
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Subject name must not be blank");
            }
            if (type == null) {
                throw new IllegalArgumentException("Subject type must not be null");
            }
            if (department == null) {
                throw new IllegalArgumentException("Department must not be null");
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

