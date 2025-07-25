package com.klef.gms.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Option text must not be blank")
    private String text;

    @Min(value = 0, message = "Marks cannot be negative")
    @Max(value = 10, message = "Marks cannot be more than 10")
    private int marks; // or isCorrect (boolean) if it's MCQ

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    private Question question;

    public boolean isMarksValid() {
        try {
            if (marks < 0 || marks > 10) {
                throw new IllegalArgumentException("Marks must be between 0 and 10");
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

