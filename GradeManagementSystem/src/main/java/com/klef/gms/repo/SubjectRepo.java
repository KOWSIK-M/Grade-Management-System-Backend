package com.klef.gms.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.klef.gms.model.Subject;

@Repository
public interface SubjectRepo extends JpaRepository<Subject, Long> {
    boolean existsByName(String name);
}