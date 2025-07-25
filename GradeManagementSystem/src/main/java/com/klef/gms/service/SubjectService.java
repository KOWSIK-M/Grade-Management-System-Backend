package com.klef.gms.service;

import com.klef.gms.model.Subject;
import com.klef.gms.model.Department;
import com.klef.gms.repo.SubjectRepo;
import com.klef.gms.repo.DepartmentRepo;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectService.class);

    private final SubjectRepo subjectRepo;
    private final DepartmentRepo departmentRepo;

    public List<Subject> findAll() {
        logger.info("Fetching all subjects.");
        try {
            List<Subject> subjects = subjectRepo.findAll();
            logger.info("Fetched {} subjects.", subjects.size());
            return subjects;
        } catch (Exception ex) {
            logger.error("Error while fetching all subjects: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch subjects", ex);
        }
    }

    public Subject save(Subject subject, Long departmentId) {
        logger.info("Attempting to save subject '{}' for departmentId: {}", subject.getName(), departmentId);
        try {
            Department dept = departmentRepo.findById(departmentId)
                    .orElseThrow(() -> {
                        logger.warn("Department not found for id: {}", departmentId);
                        return new RuntimeException("Department not found");
                    });

            subject.setDepartment(dept);

            if (subjectRepo.existsByName(subject.getName())) {
                logger.warn("Subject '{}' already exists.", subject.getName());
                throw new RuntimeException("Subject already exists");
            }

            Subject saved = subjectRepo.save(subject);
            logger.info("Subject '{}' saved successfully with id: {}", saved.getName(), saved.getId());
            return saved;
        } catch (Exception ex) {
            logger.error("Error while saving subject '{}': {}", subject.getName(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to save subject", ex);
        }
    }
    
    public long subjectCount() {
        logger.info("Counting total subjects.");
        try {
            long count = subjectRepo.count();
            logger.info("Total subjects count: {}", count);
            return count;
        } catch (Exception ex) {
            logger.error("Error while counting subjects: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to count subjects", ex);
        }
    }
}
