package com.klef.gms.model;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.*;

@Data
public class QuestionDTO {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title cannot be longer than 255 characters")
    private String title;

    @NotBlank(message = "Type must not be blank")
    private String type;

    @NotNull(message = "Subject ID must not be null")
    private Long subjectId;

    @NotNull(message = "Department ID must not be null")
    private Long departmentId;

    @NotNull(message = "Options must not be null")
    @Size(min = 1, message = "At least one option is required")
    private List<Option> options;

    /**
     * Converts this DTO to a Question entity.
     * Handles all exceptions and logs them if needed.
     */
    public Question toQuestion(Subject subject, Department department, User user) {
        try {
            Question q = Question.builder()
                    .title(this.title)
                    .type(this.type)
                    .subject(subject)
                    .department(department)
                    .createdBy(user)
                    .options(this.options)
                    .build();

            if (q.getOptions() != null) {
                q.getOptions().forEach(opt -> opt.setQuestion(q));
            }

            return q;
        } catch (Exception ex) {
            ex.printStackTrace();
            // Return a minimal Question object or handle as needed
            return Question.builder().title("Invalid Question").build();
        }
    }
}
