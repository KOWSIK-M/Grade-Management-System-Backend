package com.klef.gms.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klef.gms.model.EmailVerificationToken;
import com.klef.gms.model.User;

public interface EmailVerificationTokenRepo extends JpaRepository<EmailVerificationToken, Long> {
	Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUser(User user);
}
