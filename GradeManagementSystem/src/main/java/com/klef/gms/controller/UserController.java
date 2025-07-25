package com.klef.gms.controller;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.klef.gms.model.AuthProvider;
import com.klef.gms.model.EmailVerificationToken;
import com.klef.gms.model.PasswordResetToken;
import com.klef.gms.model.User;
import com.klef.gms.repo.EmailVerificationTokenRepo;
import com.klef.gms.repo.PasswordResetTokenRepo;
import com.klef.gms.repo.UserRepo;
import com.klef.gms.service.EmailService;
import com.klef.gms.service.JWTService;
import com.klef.gms.service.UsersService;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailVerificationTokenRepo tokenRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private JWTService jwtService;
    
    @Autowired
    private PasswordResetTokenRepo passwordResetTokenRepo;
    
    @Autowired
    private UsersService usersService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User userData) {
        logger.info("Received signup request for email: {}", userData.getEmail());
        try {
            if (userRepo.findByEmail(userData.getEmail()).isPresent()) {
                logger.warn("Attempted signup with already registered email: {}", userData.getEmail());
                return ResponseEntity.badRequest().body("Email already registered");
            }

            String token = UUID.randomUUID().toString();

            EmailVerificationToken tokenEntity = new EmailVerificationToken();
            tokenEntity.setToken(token);
            tokenEntity.setTempName(userData.getName());
            tokenEntity.setTempEmail(userData.getEmail());
            tokenEntity.setTempPassword(passwordEncoder.encode(userData.getPassword()));
            tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(15));

            tokenRepo.save(tokenEntity);

            String link = "http://localhost:5173/verify-email?token=" + token;
            emailService.send(
                userData.getEmail(),
                "Email Verification",
                "Click the link to verify your email: " + link
            );

            logger.info("Verification email sent to {}", userData.getEmail());
            return ResponseEntity.ok("Verification email sent");
        } catch (Exception ex) {
            logger.error("Error during signup: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Signup failed. Please try again.");
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        logger.info("Received signin request for email: {}", email);

        try {
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("Invalid credentials for email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }

            String token = jwtService.generateToken(email);

            ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

            logger.info("Signin successful for email: {}", email);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of("message", "Signed in successfully"));
        } catch (Exception ex) {
            logger.error("Error during signin for email {}: {}", email, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Signin failed. Please try again.");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        logger.info("Received email verification request for token: {}", token);
        try {
            Optional<EmailVerificationToken> optionalToken = tokenRepo.findByToken(token);

            if (optionalToken.isEmpty()) {
                logger.warn("Invalid or non-existent verification token: {}", token);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid or non-existent token");
            }

            EmailVerificationToken tokenEntity = optionalToken.get();

            if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
                tokenRepo.delete(tokenEntity);
                logger.warn("Verification token expired: {}", token);
                return ResponseEntity.status(HttpStatus.GONE).body("Token expired");
            }

            if (userRepo.findByEmail(tokenEntity.getTempEmail()).isPresent()) {
                tokenRepo.delete(tokenEntity);
                logger.info("Email already verified for: {}", tokenEntity.getTempEmail());
                return ResponseEntity.ok("Email already verified.");
            }

            User user = new User();
            user.setEmail(tokenEntity.getTempEmail());
            user.setName(tokenEntity.getTempName());
            user.setPassword(tokenEntity.getTempPassword());
            user.setVerified(true);
            user.setActive(true);
            user.setAuthProvider(AuthProvider.MANUAL);

            try {
                String avatarUrl = "https://ui-avatars.com/api/?name=" +
                        java.net.URLEncoder.encode(user.getName(), java.nio.charset.StandardCharsets.UTF_8) +
                        "&background=random";
                byte[] avatar = new org.springframework.web.client.RestTemplate().getForObject(avatarUrl, byte[].class);
                user.setProfileImage(avatar);
            } catch (Exception e) {
                logger.warn("Failed to fetch avatar for {}: {}", user.getName(), e.getMessage());
            }

            userRepo.save(user);
            tokenRepo.delete(tokenEntity);

            String jwt = jwtService.generateToken(user.getEmail());

            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

            logger.info("Email verified and account created for: {}", user.getEmail());
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Email verified, account created", "user", user));
        } catch (Exception ex) {
            logger.error("Error during email verification: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Email verification failed. Please try again.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Received forgot password request for email: {}", email);

        try {
            if (!userRepo.findByEmail(email).isPresent()) {
                logger.warn("Forgot password requested for unregistered email: {}", email);
                return ResponseEntity.badRequest().body("No user registered with this email.");
            }

            String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

            PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

            passwordResetTokenRepo.save(token);

            emailService.send(
                email,
                "Password Reset OTP",
                "Your OTP for password reset is: " + otp
            );

            logger.info("OTP sent to email: {}", email);
            return ResponseEntity.ok("OTP sent to email.");
        } catch (Exception ex) {
            logger.error("Error during forgot password for email {}: {}", email, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP. Please try again.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        logger.info("Received password reset request for email: {}", email);

        try {
            PasswordResetToken token = passwordResetTokenRepo
                .findByEmailAndOtp(email, otp)
                .orElse(null);

            if (token == null || token.isUsed() || token.getExpiryTime().isBefore(LocalDateTime.now())) {
                logger.warn("Invalid or expired OTP for email: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
            }

            User user = userRepo.findUserByEmail(email);
            if (user == null) {
                logger.warn("Password reset requested for non-existent user: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            if (user.getAuthProvider() == AuthProvider.GOOGLE || user.getAuthProvider() == AuthProvider.GITHUB) {
                logger.warn("Password reset attempted for social login user: {}", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Social login user. Cannot reset password.");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);

            token.setUsed(true);
            passwordResetTokenRepo.save(token);

            logger.info("Password reset successful for email: {}", email);
            return ResponseEntity.ok("Password reset successful.");
        } catch (Exception ex) {
            logger.error("Error during password reset for email {}: {}", email, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password. Please try again.");
        }
    }

    @GetMapping("/users-count")
    public Map<String, Long> getUserCount() {
        logger.info("Received request to fetch user count.");
        try {
            long count = usersService.usersCount();
            logger.info("Total users count: {}", count);
            return Collections.singletonMap("userCount", count);
        } catch (Exception ex) {
            logger.error("Error while fetching user count: {}", ex.getMessage(), ex);
            return Collections.singletonMap("userCount", 0L);
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser, Principal principal) {
        String email = principal.getName();
        logger.info("Received profile update request for email: {}", email);

        try {
            return userRepo.findByEmail(email)
                    .map(user -> {
                        String oldName = user.getName();
                        String newName = updatedUser.getName();

                        user.setName(newName);
                        user.setEmail(updatedUser.getEmail());

                        if (!oldName.equals(newName)) {
                            try {
                                String avatarUrl = "https://ui-avatars.com/api/?name=" +
                                        java.net.URLEncoder.encode(newName, java.nio.charset.StandardCharsets.UTF_8) +
                                        "&background=random";
                                byte[] avatar = new org.springframework.web.client.RestTemplate().getForObject(avatarUrl, byte[].class);
                                user.setProfileImage(avatar);
                            } catch (Exception e) {
                                logger.warn("Failed to fetch avatar for {}: {}", newName, e.getMessage());
                            }
                        }

                        userRepo.save(user);
                        logger.info("Profile updated for email: {}", email);
                        return ResponseEntity.ok("Profile updated");
                    })
                    .orElseGet(() -> {
                        logger.warn("Profile update requested for non-existent user: {}", email);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                    });
        } catch (Exception ex) {
            logger.error("Error during profile update for email {}: {}", email, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update profile. Please try again.");
        }
    }
    
    @GetMapping("/profile/image")
    public ResponseEntity<byte[]> getProfileImage(Principal principal) {
        String email = principal.getName();
        logger.info("Received request to fetch profile image for email: {}", email);

        try {
            Optional<User> optionalUser = userRepo.findByEmail(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                byte[] image = user.getProfileImage();

                if (image == null || image.length == 0) {
                    logger.warn("No profile image found for email: {}", email);
                    return ResponseEntity.notFound().build();
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setCacheControl(CacheControl.noCache());

                logger.info("Profile image returned for email: {}", email);
                return new ResponseEntity<>(image, headers, HttpStatus.OK);
            } else {
                logger.warn("Profile image requested for non-existent user: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception ex) {
            logger.error("Error while fetching profile image for email {}: {}", email, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




}
