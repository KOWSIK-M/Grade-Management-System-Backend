package com.klef.gms.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.klef.gms.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler successHandler;

    public SecurityConfig(CustomOAuth2UserService oAuth2UserService, OAuth2SuccessHandler successHandler) {
        this.oAuth2UserService = oAuth2UserService;
        this.successHandler = successHandler;
    }
    
    @Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    	http
    		.cors(Customizer.withDefaults()) 
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers(
            			    "/api/auth/signup",
            			    "/api/auth/signin",
            			    "/api/auth/me",
            			    "/api/auth/verify",
            			    "/api/auth/request-reset",
            			    "/api/auth/verify-reset-otp",
            			    "/api/auth/reset-password",
            			    "/login**",
            			    "/oauth2/**",
            			    "/api/questions/question-types",
            			    "/api/subjects/**",
            			    "/api/subjects/subject-types",
            			    "/api/departments/**",
            			    "/api/questions/**",
            			    "/api/questions/add",
            			    "/api/questions/my-questions",
            			    "/api/auth/users-count",
            			    "/api/auth/profile",
            			    "/api/questions/questions-count",
            			    "/api/subjects/subjects-count",
        					"/api/auth/profile/image",
        					"/api/auth/profile/image/"
            			).permitAll()
            			.requestMatchers( 
            					"/api/questions/**",
                			    "/api/questions/latest",
                			    "/api/auth/logout"
            			).authenticated()
            			

            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                })
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                .successHandler(successHandler)
            );
            http.httpBasic(Customizer.withDefaults());
            http.sessionManagement(session -> session
            	    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
    		http.addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
    		http.authenticationProvider(authenticationProvider());

        return http.build();
    }
    
    @Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
		provider.setUserDetailsService(userDetailsService);
		
		return provider;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
