package com.klef.gms.service;

import java.util.Collections;
import java.util.Map;

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

    private final UserRepo userRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" or "github"

        String email = null;
        String name = null;
        String picture = null;

        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");
        } else if ("github".equals(registrationId)) {
            name = (String) attributes.get("name");
            picture = (String) attributes.get("avatar_url");

            String token = userRequest.getAccessToken().getTokenValue();
            email = fetchGitHubEmail(token);
            System.out.println("Fetched GitHub email: " + email);
            System.out.println("Attributes: " + attributes);

            if ((email == null || email.isBlank()) && attributes.get("login") != null) {
                email = attributes.get("login") + "@github.com";
                System.out.println("Fallback email used: " + email);
            }

        }

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email could not be retrieved from OAuth provider.");
        }

        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        final String userEmail = email;
        final String userName = name;
        final String userPicture = picture;
        final AuthProvider userProvider = provider;

        User user = userRepo.findByEmail(userEmail).orElseGet(() ->
            userRepo.save(
                User.builder()
                    .email(userEmail)
                    .name(userName)
                    .profileImageUrl(userPicture)
                    .authProvider(userProvider)
                    .isVerified(true)
                    .active(true)
                    .build()
            )
        );
        System.out.println("Final user email being used: " + user.getEmail());

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
                        return (String) emailEntry.get("email");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch GitHub email: " + e.getMessage());
        }

        return null;
    }
}
