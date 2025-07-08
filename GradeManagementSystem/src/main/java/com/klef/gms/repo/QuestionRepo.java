package com.klef.gms.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.klef.gms.model.Question;

@Repository
public interface QuestionRepo extends JpaRepository<Question, Long> {
    List<Question> findAllByOrderByCreatedAtDesc(); // For dashboard (latest first)
}
