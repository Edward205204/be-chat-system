package com.edward.chat_system.infrastructure.configuration;

import com.edward.chat_system.infrastructure.jwt.JwtAuthenticationEntryPoint;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("java:S4502") // sonar
public class SecurityConfig {
    static final String[] TEMP_ENDPOINTS = {"/auth/send/email-otp", "/auth/verify/email-otp"};
    static final String[] PUBLIC_ENDPOINTS = {
        "/auth/token", "/auth/logout", "/auth/refresh", "/auth/register", "/auth/login"
    };

    JwtDecoder accessTokenDecoder;
    JwtDecoder tmpTokenDecoder;

    public SecurityConfig(
            @Qualifier("accessTokenDecoder") JwtDecoder accessTokenDecoder,
            @Qualifier("tmpTokenDecoder") JwtDecoder tmpTokenDecoder) {
        this.accessTokenDecoder = accessTokenDecoder;
        this.tmpTokenDecoder = tmpTokenDecoder;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain tmpTokenFilterChain(HttpSecurity http) {
        return http.securityMatcher(TEMP_ENDPOINTS)
                .authorizeHttpRequests(req -> req.anyRequest().authenticated())
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                                jwt ->
                                                        jwt.decoder(tmpTokenDecoder)
                                                                .jwtAuthenticationConverter(
                                                                        jwtAuthenticationConverter()))
                                        .authenticationEntryPoint(
                                                new JwtAuthenticationEntryPoint()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) {
        return http.authorizeHttpRequests(
                        req ->
                                req.requestMatchers(PUBLIC_ENDPOINTS)
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                                jwt ->
                                                        jwt.decoder(accessTokenDecoder)
                                                                .jwtAuthenticationConverter(
                                                                        jwtAuthenticationConverter()))
                                        .authenticationEntryPoint(
                                                new JwtAuthenticationEntryPoint()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource =
                new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
