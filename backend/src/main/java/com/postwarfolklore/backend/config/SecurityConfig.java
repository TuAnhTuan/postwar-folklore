package com.postwarfolklore.backend.config;

import com.postwarfolklore.backend.security.FirebaseAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Cho phép preflight OPTIONS đi qua
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public: GET tất cả bài viết, bình luận và địa điểm
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/locations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/ai/prompt").permitAll()
                // Public: Chat AI
                .requestMatchers(HttpMethod.POST, "/api/ai/chat").permitAll()
                // Setup: cấp quyền admin lần đầu (bảo vệ bằng secret)
                .requestMatchers(HttpMethod.POST, "/api/setup/admin").permitAll()
                // Public: POST comment (cả Guest lẫn Auth, phân biệt trong service)
                .requestMatchers(HttpMethod.POST, "/api/comments/**").permitAll()
                // Admin only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Phần còn lại yêu cầu đăng nhập
                .anyRequest().authenticated()
            )
            .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
