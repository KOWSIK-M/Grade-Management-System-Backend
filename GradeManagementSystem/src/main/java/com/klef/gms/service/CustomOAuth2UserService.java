package com.klef.gms.service;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.klef.gms.model.AuthProvider;
import com.klef.gms.model.User;
import com.klef.gms.repo.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepo userRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("Starting OAuth2 user loading process.");
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User;
        try {
            oAuth2User = delegate.loadUser(userRequest);
        } catch (Exception ex) {
            logger.error("Failed to load user from OAuth2 provider: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException("Failed to load user from OAuth2 provider.");
        }

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" or "github"

        String email = null;
        String name = null;
        String picture = null;

        try {
            if ("google".equals(registrationId)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                picture = (String) attributes.get("picture");
                logger.debug("Google OAuth2 user: email={}, name={}", email, name);
            } else if ("github".equals(registrationId)) {
                name = (String) attributes.get("name");
                picture = (String) attributes.get("avatar_url");

                String token = userRequest.getAccessToken().getTokenValue();
                email = fetchGitHubEmail(token);
                logger.debug("GitHub OAuth2 user: name={}, fetched email={}", name, email);

                if ((email == null || email.isBlank()) && attributes.get("login") != null) {
                    email = attributes.get("login") + "@github.com";
                    logger.warn("Fallback email used for GitHub: {}", email);
                }
            }
        } catch (Exception ex) {
            logger.error("Error extracting user info from OAuth2 attributes: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException("Error extracting user info from OAuth2 attributes.");
        }

        if (email == null || email.isBlank()) {
            logger.error("Email could not be retrieved from OAuth provider.");
            throw new OAuth2AuthenticationException("Email could not be retrieved from OAuth provider.");
        }

        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        final String userEmail = email;
        final String userName = name;
        final String userPicture = picture;
        final AuthProvider userProvider = provider;

        User user;
        try {
            user = userRepo.findByEmail(userEmail).orElseGet(() -> {
                logger.info("No user found with email {}. Creating new user.", userEmail);
                return userRepo.save(
                    User.builder()
                        .email(userEmail)
                        .name(userName)
                        .profileImageUrl(userPicture)
                        .authProvider(userProvider)
                        .isVerified(true)
                        .active(true)
                        .build()
                );
            });
            logger.info("User loaded or created: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("Error saving or retrieving user: {}", ex.getMessage(), ex);
            throw new OAuth2AuthenticationException("Error saving or retrieving user.");
        }

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of(
                "email", user.getEmail(),
                "name", user.getName(),
                "picture", user.getProfileImageUrl()
            ),
            "email"  // this is the key Spring will use as the "name attribute"
        );
    }

    private String fetchGitHubEmail(String token) {
        logger.info("Fetching GitHub email using access token.");
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>("", headers);

            ResponseEntity<Object[]> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                Object[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                for (Object obj : response.getBody()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> emailEntry = (Map<String, Object>) obj;
                    Boolean primary = (Boolean) emailEntry.get("primary");
                    Boolean verified = (Boolean) emailEntry.get("verified");
                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        logger.info("Primary and verified GitHub email found: {}", emailEntry.get("email"));
                        return (String) emailEntry.get("email");
                    }
                }
                logger.warn("No primary and verified GitHub email found.");
            } else {
                logger.warn("Failed to fetch GitHub emails. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Failed to fetch GitHub email: {}", e.getMessage(), e);
        }
        return null;
    }
}
