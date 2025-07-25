package com.klef.gms.model;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.*;

public class UserPrincipal implements UserDetails {

    private static final Logger logger = LoggerFactory.getLogger(UserPrincipal.class);

    @NotNull(message = "User must not be null")
    private User user;

    public UserPrincipal(User user) {
        if (user == null) {
            logger.error("UserPrincipal initialization failed: user is null");
            throw new IllegalArgumentException("User must not be null");
        }
        this.user = user;
        logger.info("UserPrincipal created for user: {}", user.getEmail());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        try {
            return Collections.singleton(new SimpleGrantedAuthority("USER"));
        } catch (Exception ex) {
            logger.error("Error getting authorities: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    @Override
    public String getPassword() {
        try {
            return user.getPassword();
        } catch (Exception ex) {
            logger.error("Error getting password for user: {}", ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public String getUsername() {
        try {
            // Use email as username for authentication
            return user.getEmail();
        } catch (Exception ex) {
            logger.error("Error getting username for user: {}", ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public boolean isAccountNonExpired() {
        // You can add logic here if needed
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // You can add logic here if needed
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // You can add logic here if needed
        return true;
    }

    @Override
    public boolean isEnabled() {
        try {
            return user.isActive();
        } catch (Exception ex) {
            logger.error("Error checking if user is enabled: {}", ex.getMessage(), ex);
            return false;
        }
    }
}
