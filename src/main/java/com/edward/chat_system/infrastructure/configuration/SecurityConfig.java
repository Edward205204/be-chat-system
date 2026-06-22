package com.edward.chat_system.infrastructure.configuration;

import com.edward.chat_system.infrastructure.jwt.JwtAuthenticationEntryPoint;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    @NonFinal
    @Value("${jwt.signerKey}")
    String signerKey;

    static final String TEMT_ENDPOINT = "/auth/send-otp";
    static final String[] PUBLIC_ENDPOINTS = {
        "/users", "/auth/token", "/auth/introspect", "/auth/logout", "/auth/refresh"
    };

    @NonFinal JwtDecoder accessTokenDecoder;
    @NonFinal JwtDecoder tmpTokenDecoder;

    public SecurityConfig(
            @Qualifier("accessTokenDecoder") JwtDecoder accessTokenDecoder,
            @Qualifier("tmpTokenDecoder") JwtDecoder tmpTokenDecoder) {
        this.accessTokenDecoder = accessTokenDecoder;
        this.tmpTokenDecoder = tmpTokenDecoder;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain tmpTokenFilterChain(HttpSecurity http) {
        return http.securityMatcher(TEMT_ENDPOINT)
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
