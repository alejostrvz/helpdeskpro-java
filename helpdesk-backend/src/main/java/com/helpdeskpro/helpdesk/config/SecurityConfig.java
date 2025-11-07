package com.helpdeskpro.helpdesk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Profile({"default", "dev", "test"}) // Se aplica en desarrollo y tests
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                  // Para POST desde Postman
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // Permitir todo
                .httpBasic(httpBasic -> httpBasic.disable())   // Desactivar HTTP Basic
                .formLogin(form -> form.disable());            // Desactivar login por formulario

        return http.build();
    }
}
