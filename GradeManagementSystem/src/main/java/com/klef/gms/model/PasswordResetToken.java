package com.klef.gms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "OTP must not be blank")
    @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit number")
    private String otp;

    @NotNull(message = "Expiry time must not be null")
    private LocalDateTime expiryTime;

    private boolean used = false;

    /**
     * Checks if the token is expired.
     * Handles all exceptions and returns true if expired or error occurs.
     */
    public boolean isExpired() {
        try {
            if (expiryTime == null) {
                throw new IllegalStateException("Expiry time is not set");
            }
            return expiryTime.isBefore(LocalDateTime.now());
        } catch (Exception ex) {
            ex.printStackTrace();
            return true; // Treat as expired if any error occurs
        }
    }
}
