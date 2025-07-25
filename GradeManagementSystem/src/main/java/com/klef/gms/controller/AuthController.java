package com.klef.gms.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.klef.gms.model.AuthProvider;
import com.klef.gms.model.EmailVerificationToken;
import com.klef.gms.model.PasswordResetToken;
import com.klef.gms.model.User;
import com.klef.gms.repo.EmailVerificationTokenRepo;
import com.klef.gms.repo.PasswordResetTokenRepo;
import com.klef.gms.repo.UserRepo;
import com.klef.gms.service.EmailService;
import com.klef.gms.service.JWTService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepo userRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;
    private final EmailService emailService;
    private final JWTService jwtService;

    
    @Autowired 
    private JavaMailSender mailSender;
    @Autowired 
    private PasswordResetTokenRepo tokenRepo;

    // Utility method to create JWT HttpOnly cookie
    private Cookie createJwtCookie(String jwt) {
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // use HTTPS in production
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 1); // 1 day
        return cookie;
    }

    // GET user profile after OAuth login (Google)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal Object principal,
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.info("Received request for current user profile.");

        try {
            // Case 1: OAuth2 (Google/GitHub) session-based login
            if (principal instanceof OAuth2User oAuth2User) {
                String email = oAuth2User.getAttribute("email");
                String name = oAuth2User.getAttribute("name");
                String pictureUrl = oAuth2User.getAttribute("picture");
                String avatarUrl = oAuth2User.getAttribute("avatar_url");

                if (email == null) {
                    logger.warn("Email not available from OAuth provider.");
                    return ResponseEntity.status(400).body("Email not available from OAuth provider");
                }

                User user = userRepo.findByEmail(email).orElse(null);

                if (user == null) {
                    user = new User();
                    user.setEmail(email);
                    user.setName(name);
                    user.setAuthProvider(oAuth2User.getAttribute("sub") != null ? AuthProvider.GOOGLE : AuthProvider.GITHUB);
                    user.setVerified(false);
                    user.setActive(true);

                    try {
                        String imageUrl = pictureUrl != null ? pictureUrl : avatarUrl;
                        if (imageUrl != null) {
                            logger.info("Fetching profile image from: {}", imageUrl);
                            byte[] imageBytes = new RestTemplate().getForObject(imageUrl, byte[].class);
                            user.setProfileImage(imageBytes);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to fetch profile image: {}", e.getMessage(), e);
                    }

                    user = userRepo.save(user);

                    // Email Verification
                    String token = UUID.randomUUID().toString();
                    EmailVerificationToken verificationToken = new EmailVerificationToken(
                            null, token, user, token, token, token,
                            LocalDateTime.now().plusMinutes(15)
                    );
                    emailVerificationTokenRepo.save(verificationToken);

                    String link = "http://localhost:5173/verify-email?token=" + token;
                    emailService.send(email, "Email Verification", "Click this link to verify your email: " + link);
                    logger.info("Sent email verification link to {}", email);
                }

                // Send a JWT cookie for SPA frontend convenience
                String jwt = jwtService.generateToken(email);
                response.addCookie(createJwtCookie(jwt));
                logger.info("JWT cookie set for user: {}", email);

                return ResponseEntity.ok(user);
            }

            // Case 2: JWT-based manual login
            if (principal instanceof UserDetails userDetails) {
                String email = userDetails.getUsername();
                User user = userRepo.findByEmail(email).orElse(null);
                if (user != null) {
                    logger.info("User found for email: {}", email);
                    return ResponseEntity.ok(user);
                } else {
                    logger.warn("User not found for email: {}", email);
                    return ResponseEntity.status(404).body("User not found");
                }
            }

            logger.warn("User not authenticated.");
            return ResponseEntity.status(401).body("User not authenticated");

        } catch (Exception ex) {
            logger.error("Exception in getCurrentUser: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }



    // LOGOUT: clear JWT cookie and redirect
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate session (OAuth)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear authentication context
        SecurityContextHolder.clearContext();

        // Delete JWT cookie (if present)
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(true); // Set to false if not using HTTPS locally
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/request-reset")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> req) {
        String email = req.get("email");

        // Check if user exists
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not registered");
        }

        User user = userOpt.get();

        // 🚫 Block if login type is GOOGLE or GITHUB (only allow MANUAL)
        if (!user.getAuthProvider().name().equalsIgnoreCase("MANUAL")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("This account is registered via " + user.getAuthProvider().name() + ". Please use that method to sign in.");
        }

        // ✅ Generate 6-digit OTP (remove this part if you no longer want to use OTP)
        String otp = String.valueOf(new Random().nextInt(899999) + 100000);

        // Save OTP to DB (overwrite if exists)
        tokenRepo.deleteByEmail(email);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        tokenRepo.save(token);

        // Send OTP Email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP is: " + otp + "\nIt expires in 5 minutes.");
        mailSender.send(message);

        return ResponseEntity.ok("OTP sent");
    }




    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");

        Optional<PasswordResetToken> tokenOpt = tokenRepo.findByEmailAndOtp(email, otp);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }

        PasswordResetToken token = tokenOpt.get();
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP expired");
        }

        return ResponseEntity.ok("OTP verified");
    }
}
