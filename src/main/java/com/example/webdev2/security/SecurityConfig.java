package com.example.webdev2.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import java.io.IOException;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider,
            CsrfTokenRepository csrfTokenRepository,
            ObjectMapper objectMapper) throws Exception {

        AccessDeniedHandler deniedHandler = (request, response, exception) ->
                writeJson(response, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Check the account role and CSRF token.");

        AuthenticationEntryPoint entryPoint = (request, response, exception) ->
                writeJson(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentication is required. Please sign in.");

        http
                .authenticationProvider(authenticationProvider)
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
        .csrfTokenRepository(csrfTokenRepository)
        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/api/auth/csrf", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("READER", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("MANAGER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) ->
                                writeJson(response, objectMapper, HttpServletResponse.SC_OK,
                                        currentUserPayload(authentication)))
                        .failureHandler((request, response, exception) ->
                                writeJson(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Invalid username or password."))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) ->
                                writeJson(response, objectMapper, HttpServletResponse.SC_OK,
                                        Map.of("message", "Signed out successfully."))))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.migrateSession()));

        return http.build();
    }

    private static Map<String, Object> currentUserPayload(Authentication authentication) {
        var roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .sorted()
                .toList();
        return Map.of("username", authentication.getName(), "roles", roles);
    }

    private static void writeJson(HttpServletResponse response, ObjectMapper mapper,
                                  int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), body instanceof String
                ? Map.of("message", body) : body);
    }
}
