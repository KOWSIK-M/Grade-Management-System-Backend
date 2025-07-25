package com.klef.gms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.klef.gms.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

    private final UserRepo userRepo;

    public long usersCount() {
        logger.info("Fetching total users count.");
        try {
            long count = userRepo.count();
            logger.info("Total users count: {}", count);
            return count;
        } catch (Exception ex) {
            logger.error("Error while counting users: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to count users", ex);
        }
    }
}
