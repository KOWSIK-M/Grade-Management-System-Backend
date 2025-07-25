package com.klef.gms.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title cannot be longer than 255 characters")
    private String title;

    @NotBlank(message = "Type must not be blank")
    private String type;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    @NotNull(message = "Subject must not be null")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @NotNull(message = "Department must not be null")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @NotNull(message = "Created by user must not be null")
    private User createdBy;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Size(min = 1, message = "At least one option is required")
    private List<Option> options;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        try {
            this.createdAt = new Date();
        } catch (Exception ex) {
            ex.printStackTrace();
            // If exception occurs, fallback to current time
            this.createdAt = new Date(System.currentTimeMillis());
        }
    }
}
