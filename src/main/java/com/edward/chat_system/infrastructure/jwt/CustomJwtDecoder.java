package com.edward.chat_system.infrastructure.jwt;

import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtDecoder jwtDecoder;

    public CustomJwtDecoder(@Value("${jwt.signerKey}") String signerKey) {
        SecretKeySpec secretKey =
                new SecretKeySpec(signerKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS512).build();

        decoder.setJwtValidator(JwtValidators.createDefault());

        this.jwtDecoder = decoder;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        return jwtDecoder.decode(token);
    }
}
