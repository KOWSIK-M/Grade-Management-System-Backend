package com.klef.gms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider authProvider;

    private boolean isVerified;

    @Column(nullable = false)
    private boolean active = true;

    @Lob
    @Column(name = "profile_image", columnDefinition = "LONGBLOB")
    private byte[] profileImage;

    /**
     * Example method to demonstrate exception handling and clear logic.
     * Checks if the user is valid according to business rules.
     */
    public boolean isValidUser() {
        try {
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (password != null && password.length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }
            if (authProvider == null) {
                throw new IllegalArgumentException("AuthProvider must not be null");
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
