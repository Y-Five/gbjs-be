/*
 * Copyright (c) 2025 YFIVE
 */
package com.yfive.gbjs.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.yfive.gbjs.global.config.jwt.JwtFilter;
import com.yfive.gbjs.global.config.jwt.JwtTokenProvider;
import com.yfive.gbjs.global.security.CustomOAuth2UserService;
import com.yfive.gbjs.global.security.OAuth2LoginSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtTokenProvider tokenProvider;
  private final CustomOAuth2UserService oauth2UserService;
  private final OAuth2LoginSuccessHandler customSuccessHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(
            headers ->
                headers
                    .frameOptions(FrameOptionsConfig::sameOrigin)
                    .contentSecurityPolicy(
                        csp ->
                            csp.policyDirectives(
                                "default-src 'self'; script-src 'self'; img-src 'self'; style-src 'self';")))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Actuator 엔드포인트
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    // Swagger UI
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                    // 공개 API
                    .requestMatchers(
                        "/api/auth/**",
                        "/api/audio-guide/**",
                        "/api/weathers/**",
                        "/api/festivals/**",
                        "/api/seals",
                        "/api/seals/products",
                        "/api/seals/nearby",
                        "/api/spots/**",
                        "/api/courses/**",
                        "/api/users/**",
                        "/api/traditions/**",
                        "/api/tts/**")
                    .permitAll()
                    // H2 콘솔
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    // 기타 모든 요청은 인증 필요
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(new JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)
        .oauth2Login(
            oauth2 ->
                oauth2
                    .userInfoEndpoint(
                        userInfo -> userInfo.userService(oauth2UserService) // 사용자 정보 처리
                        )
                    .successHandler(customSuccessHandler) // 로그인 성공 처리
            );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://yourfrontend.com"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
    configuration.setExposedHeaders(List.of("Authorization"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
