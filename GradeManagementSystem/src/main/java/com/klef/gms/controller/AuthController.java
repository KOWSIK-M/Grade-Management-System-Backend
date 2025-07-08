package com.klef.gms.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.klef.gms.model.AuthProvider;
import com.klef.gms.model.EmailVerificationToken;
import com.klef.gms.model.User;
import com.klef.gms.repo.EmailVerificationTokenRepo;
import com.klef.gms.repo.UserRepo;
import com.klef.gms.service.EmailService;
import com.klef.gms.service.JWTService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // ✅ Needed for cookies
public class AuthController {

    private final UserRepo userRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;
    private final EmailService emailService;
    private final JWTService jwtService;

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
            HttpServletResponse response) {

        if (principal instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");

            User user = userRepo.findByEmail(email).orElse(null);

            if (user == null) {
                // New user - create and save
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setProfileImageUrl(picture);
                newUser.setAuthProvider(AuthProvider.GOOGLE);
                newUser.setVerified(false);
                newUser.setActive(true);
                user = userRepo.save(newUser);

                // Generate verification token and send email
                String token = UUID.randomUUID().toString();
                EmailVerificationToken verificationToken = new EmailVerificationToken();
                verificationToken.setToken(token);
                verificationToken.setUser(user);
                verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

                emailVerificationTokenRepo.save(verificationToken);

                String link = "http://localhost:5173/verify-email?token=" + token;
                emailService.send(
                    user.getEmail(),
                    "Email Verification",
                    "Click this link to verify your email: " + link
                );
            }

            // ✅ Generate JWT and set as HttpOnly cookie
            String jwt = jwtService.generateToken(email);
            response.addCookie(createJwtCookie(jwt));

            return ResponseEntity.ok(user);
        }

        return ResponseEntity.status(401).body("User not authenticated");
    }

    // LOGOUT: clear JWT cookie and redirect
    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.logout();

        // Clear cookie
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        response.sendRedirect("http://localhost:5173/signin");
    }
}
