package com.factoryreceipt.factoryreceipt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Publiczne zasoby – wszystkie GET na te endpointy są dozwolone
                        .requestMatchers(HttpMethod.GET,
                                "/", "/index.html", "/login.html","/login", "/main", "/css/**", "/js/**", "/images/**", "/admin.html", "/uploads/**").permitAll()
                        // Konsola H2
                        .requestMatchers("/h2-console/**").permitAll()
                        // Publiczne operacje (logowanie, rejestracja, itd.)
                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/createAccount").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/account/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/account/updateEmail").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/shops").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/shops/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/uploadImage").permitAll()
                        // Pozostałe endpointy wymagają autoryzacji
                        .anyRequest().authenticated()
                )
                // Pozwól na wyświetlanie konsoli H2 w ramce
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // Ustawienie sesji: tworzymy sesję, jeśli jest potrzebna, a jednocześnie ograniczamy liczbę sesji do 1
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
