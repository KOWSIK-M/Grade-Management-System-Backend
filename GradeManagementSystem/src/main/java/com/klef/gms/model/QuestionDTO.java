package com.klef.gms.model;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private String title;
    private String type;
    private Long subjectId;
    private Long departmentId;
    private List<Option> options;

    public Question toQuestion(Subject subject, Department department, User user) {
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
    }
}
