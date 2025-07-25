package com.klef.gms.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

    @Value("${app.jwtSecret}")
    private String secretkey;

    public String generateToken(String usernameOrEmail) {
        logger.info("Generating JWT token for user: {}", usernameOrEmail);
        try {
            Map<String, Object> claims = new HashMap<>();
            String token = Jwts.builder()
                    .claims()
                    .add(claims)
                    .subject(usernameOrEmail)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                    .and()
                    .signWith(getKey(), Jwts.SIG.HS512)
                    .compact();
            logger.info("JWT token generated successfully for user: {}", usernameOrEmail);
            return token;
        } catch (Exception ex) {
            logger.error("Failed to generate JWT token for user {}: {}", usernameOrEmail, ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate JWT token", ex);
        }
    }

    private SecretKey getKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretkey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception ex) {
            logger.error("Failed to decode JWT secret key: {}", ex.getMessage(), ex);
            throw new RuntimeException("Invalid JWT secret key", ex);
        }
    }

    public String extractUserName(String token) {
        logger.info("Extracting username from JWT token.");
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception ex) {
            logger.error("Failed to extract username from token: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to extract username from token", ex);
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimResolver.apply(claims);
        } catch (Exception ex) {
            logger.error("Failed to extract claim from token: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to extract claim from token", ex);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            logger.error("Failed to extract all claims from token: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to extract claims from token", ex);
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        logger.info("Validating JWT token for user: {}", userDetails.getUsername());
        try {
            final String userName = extractUserName(token);
            boolean isValid = (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
            if (isValid) {
                logger.info("JWT token is valid for user: {}", userDetails.getUsername());
            } else {
                logger.warn("JWT token is invalid or expired for user: {}", userDetails.getUsername());
            }
            return isValid;
        } catch (Exception ex) {
            logger.error("Failed to validate token: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception ex) {
            logger.error("Failed to check if token is expired: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to check token expiration", ex);
        }
    }

    private Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception ex) {
            logger.error("Failed to extract expiration from token: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to extract expiration from token", ex);
        }
    }

}