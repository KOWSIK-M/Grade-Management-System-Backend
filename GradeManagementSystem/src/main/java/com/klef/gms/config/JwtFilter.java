package com.klef.gms.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.klef.gms.service.JWTService;
import com.klef.gms.service.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JWTService jwtservice;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        String useremail = null;

        // Extract JWT from Header or Cookie
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("jwt")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Extract user email from token
        if (token != null) {
            try {
                useremail = jwtservice.extractUserName(token);
                System.out.println("✅ JWT Email Extracted: " + useremail);
            } catch (Exception e) {
                System.out.println("❌ Failed to extract username from token: " + e.getMessage());
            }
        }

        // Authenticate only if username extracted and not already authenticated
        if (useremail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                MyUserDetailsService userDetailsService = context.getBean(MyUserDetailsService.class);
                UserDetails userDetails = userDetailsService.loadUserByUsername(useremail);

                if (jwtservice.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ JWT Authentication Successful for: " + useremail);
                }
            } catch (UsernameNotFoundException e) {
                System.out.println("❌ UserNotFoundException: " + useremail);
                // Optionally: response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            } catch (Exception e) {
                System.out.println("❌ Error during authentication: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
