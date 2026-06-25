package com.edward.chat_system.infrastructure.security.jwt;

import com.edward.chat_system.shared.enums.TokenTypeEnum;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtDecoderConfig {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Bean
    public JwtDecoder accessTokenDecoder() {
        return buildDecoder(TokenTypeEnum.ACCESS_TOKEN);
    }

    @Bean
    public JwtDecoder tmpTokenDecoder() {
        return buildDecoder(TokenTypeEnum.TMP_TOKEN);
    }

    private JwtDecoder buildDecoder(TokenTypeEnum tokenType) {
        SecretKeySpec secretKey =
                new SecretKeySpec(signerKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS512).build();

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefault(),
                        jwt ->
                                tokenType.name().equals(jwt.getClaimAsString("tokenType"))
                                        ? OAuth2TokenValidatorResult.success()
                                        : OAuth2TokenValidatorResult.failure(
                                                new OAuth2Error("invalid_token"))));

        return decoder;
    }
}
