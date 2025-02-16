package com.factoryreceipt.factoryreceipt.config;

import com.factoryreceipt.factoryreceipt.filter.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Zezwól na statyczne zasoby i strony publiczne
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/login.html", "/css/**", "/js/**", "/images/**", "/admin.html").permitAll()
                        // Zezwól na dostęp do konsoli H2
                        .requestMatchers("/h2-console/**").permitAll()
                        // Zezwól na logowanie oraz tworzenie kont
                        .requestMatchers(HttpMethod.POST, "/api/login", "/api/createAccount").permitAll()
                        // Zezwól na odczyt danych konta
                        .requestMatchers(HttpMethod.GET, "/api/account/**").permitAll()
                        // Zezwól na wysyłkę potwierdzeń
                        .requestMatchers(HttpMethod.POST, "/api/shops/stockx").permitAll()
                        // Zezwól na aktualizację e-maila (np. publicznie – możesz to zabezpieczyć inaczej, jeśli chcesz)
                        .requestMatchers(HttpMethod.PUT, "/api/account/updateEmail").permitAll()
                        // Pozostałe żądania wymagają autoryzacji
                        .anyRequest().authenticated()
                )
                // Pozwól na wyświetlanie konsoli H2 w ramce
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // Ustaw sesję jako bezstanową (JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
