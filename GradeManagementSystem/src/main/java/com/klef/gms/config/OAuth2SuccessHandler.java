package com.klef.gms.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.klef.gms.service.JWTService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JWTService jwtService;

    public OAuth2SuccessHandler() {
        super("http://localhost:5173/dashboard"); // redirect to frontend
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // ✅ Extract OAuth2User
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");  // ✅ actual email

        // 🔐 Generate JWT
        String token = jwtService.generateToken(email);

        // 🍪 Set JWT as cookie
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(15 * 60); // 15 minutes
        cookie.setPath("/");
        cookie.setSecure(true); // ✅ Important for SameSite=None to work on Chrome/Firefox
        cookie.setDomain("localhost"); // Optional: needed only if dealing with subdomains
        cookie.setComment("SameSite=None; Secure"); // For older servlet containers

        response.addHeader("Set-Cookie", String.format("jwt=%s; Path=/; Max-Age=900; HttpOnly; Secure; SameSite=None", token)); // ✅ force SameSite=None

        // 🧼 Optionally reset Spring session
        request.getSession().invalidate();

        // 🔁 Redirect as normal
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
