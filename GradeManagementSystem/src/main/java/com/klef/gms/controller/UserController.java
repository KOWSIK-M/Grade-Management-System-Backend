package com.klef.gms.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.klef.gms.model.AuthProvider;
import com.klef.gms.model.EmailVerificationToken;
import com.klef.gms.model.User;
import com.klef.gms.repo.EmailVerificationTokenRepo;
import com.klef.gms.repo.UserRepo;
import com.klef.gms.service.EmailService;
import com.klef.gms.service.JWTService;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

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

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User userData) {
        if (userRepo.findByEmail(userData.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        // Generate verification token
        String token = UUID.randomUUID().toString();

        EmailVerificationToken tokenEntity = new EmailVerificationToken();
        tokenEntity.setToken(token);
        tokenEntity.setTempName(userData.getName());
        tokenEntity.setTempEmail(userData.getEmail());
        tokenEntity.setTempPassword(passwordEncoder.encode(userData.getPassword()));
        tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        tokenRepo.save(tokenEntity);

        // Send email
        String link = "http://localhost:5173/verify-email?token=" + token;
        emailService.send(
            userData.getEmail(),
            "Email Verification",
            "Click the link to verify your email: " + link
        );

        return ResponseEntity.ok("Verification email sent");
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtService.generateToken(email); // generate JWT

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
            .httpOnly(true)
            .secure(true) // use false if testing on localhost without https
            .path("/")
            .maxAge(24 * 60 * 60) // 1 day
            .sameSite("Lax")
            .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Signed in successfully"));
    }


    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        EmailVerificationToken tokenEntity = tokenRepo.findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));

        if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        if (userRepo.findByEmail(tokenEntity.getTempEmail()).isPresent()) {
            tokenRepo.delete(tokenEntity);
            return ResponseEntity.ok("Email already verified.");
        }

        User user = new User();
        user.setEmail(tokenEntity.getTempEmail());
        user.setName(tokenEntity.getTempName());
        user.setPassword(tokenEntity.getTempPassword());
        user.setVerified(true);
        user.setActive(true);
        user.setAuthProvider(AuthProvider.MANUAL);

        userRepo.save(user);
        tokenRepo.delete(tokenEntity);

        // ✅ generate JWT token here
        String jwt = jwtService.generateToken(user.getEmail());

        return ResponseEntity.ok(Map.of(
            "message", "Email verified, account created",
            "token", jwt,
            "user", user
        ));
    }



}
