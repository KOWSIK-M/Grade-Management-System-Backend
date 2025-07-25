package com.klef.gms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class GradeManagementSystemApplication {

    private static final Logger logger = LoggerFactory.getLogger(GradeManagementSystemApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Grade Management System Application...");
        try {
            SpringApplication.run(GradeManagementSystemApplication.class, args);
            logger.info("Grade Management System Application started successfully.");
        } catch (Exception ex) {
            logger.error("Application failed to start: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

}
