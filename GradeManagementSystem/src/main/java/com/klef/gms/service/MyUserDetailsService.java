package com.klef.gms.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.klef.gms.model.User;
import com.klef.gms.repo.UserRepo;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user by username (email): {}", username);
        try {
            return userRepo.findByEmail(username)
                    .map(user -> {
                        logger.info("User found for email: {}", username);
                        return org.springframework.security.core.userdetails.User.builder()
                                .username(user.getEmail())
                                .password(user.getPassword() != null ? user.getPassword() : "")
                                .roles("USER")
                                .build();
                    })
                    .orElseThrow(() -> {
                        logger.warn("User not found for email: {}", username);
                        return new UsernameNotFoundException("User not found: " + username);
                    });
        } catch (Exception ex) {
            logger.error("Exception occurred while loading user by username: {}", ex.getMessage(), ex);
            throw new UsernameNotFoundException("Error loading user: " + username, ex);
        }
    }
}
