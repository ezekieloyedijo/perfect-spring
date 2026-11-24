package com.perfectspring.topic05_security.project;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * THE GATE: who exists, how passwords are stored, and the RULES POSTER.
 */
@Configuration
@EnableMethodSecurity            // allows @PreAuthorize on methods (the "extra lock")
public class SecurityConfig {

    /** The meat grinder: passwords are stored as BCrypt hashes, never as plain text. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * The REGISTER of ID-card holders.
     * In a real app you would load users from a database instead.
     */
    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("ada").password(encoder.encode("ada123")).roles("STUDENT").build(),
                User.withUsername("tunde").password(encoder.encode("tunde123")).roles("STUDENT").build(),
                User.withUsername("mrbello").password(encoder.encode("teach123")).roles("TEACHER").build(),
                User.withUsername("admin").password(encoder.encode("admin123")).roles("ADMIN").build()
        );
    }

    /**
     * The RULES POSTER at the gate. Read TOP to BOTTOM: the first matching rule wins.
     */
    @Bean
    public SecurityFilterChain rules(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()                         // anyone at all
                .requestMatchers(HttpMethod.GET, "/results/my").hasRole("STUDENT") // green cards
                .requestMatchers(HttpMethod.POST, "/results").hasRole("TEACHER")   // blue cards
                .requestMatchers("/admin/**").hasRole("ADMIN")                     // red cards
                .anyRequest().authenticated()                                      // everything else: any valid card
            )
            .httpBasic(Customizer.withDefaults())   // log in by sending username + password with each request
            .csrf(csrf -> csrf.disable());          // OK for this tool-driven API. Keep it ON for cookie-based websites!

        return http.build();
    }
}
