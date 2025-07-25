package com.klef.gms.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Token must not be blank")
    private String token;

    @OneToOne
    private User user;

    @NotBlank(message = "Temporary name must not be blank")
    private String tempName;

    @NotBlank(message = "Temporary email must not be blank")
    private String tempEmail;

    @NotBlank(message = "Temporary password must not be blank")
    private String tempPassword;

    @NotNull(message = "Expiry date must not be null")
    private LocalDateTime expiryDate;

    public boolean isExpired() {
        try {
            if (expiryDate == null) {
                throw new IllegalStateException("Expiry date is not set");
            }
            return expiryDate.isBefore(LocalDateTime.now());
        } catch (Exception ex) {
            // Log exception if needed
            ex.printStackTrace();
            return true; // Treat as expired if any error occurs
        }
    }
}
