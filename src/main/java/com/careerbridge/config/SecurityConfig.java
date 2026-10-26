package com.careerbridge.config;

import com.careerbridge.security.CustomUserDetailsService;
import com.careerbridge.security.JwtAuthenticationEntryPoint;
import com.careerbridge.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

/**
 * Spring Security Configuration for CareerBridge.
 * 
 * Configures:
 * 1. Stateless session management (SessionCreationPolicy.STATELESS) for REST APIs.
 * 2. Cross-Origin Resource Sharing (CORS) permitting React frontend requests.
 * 3. CSRF disabled since authentication uses stateless JSON Web Tokens (JWT).
 * 4. Role-based authorization rules and public endpoint permissions.
 * 5. Custom JwtAuthenticationFilter integration before UsernamePasswordAuthenticationFilter.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    /**
     * Bean definition for custom JWT authentication filter that extracts and validates tokens.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Exposes the Spring AuthenticationManager bean used during user login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Password encoder using BCrypt hashing algorithm for secure credential storage.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Main security filter chain establishing HTTP endpoint access rules and filters.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configure CORS to allow communication with frontend development and production servers
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration corsConfiguration = new CorsConfiguration();
                corsConfiguration.setAllowedOriginPatterns(List.of("*"));
                corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                corsConfiguration.setAllowedHeaders(List.of("*"));
                corsConfiguration.setAllowCredentials(true);
                return corsConfiguration;
            }))
            // Disable CSRF as REST APIs rely on stateless JWT Bearer tokens
            .csrf(AbstractHttpConfigurer::disable)
            // Handle unauthorized access attempts with custom 401 response
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            // Enforce stateless session management (no server-side HTTP session storage)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Define URL access rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login",
                    "/register",
                    "/dashboard",
                    "/applications",
                    "/jobs",
                    "/resumes",
                    "/admin",
                    "/recruiter",
                    "/assets/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            );

        // Register custom JWT filter in the Spring Security filter chain
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
