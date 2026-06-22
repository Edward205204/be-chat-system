package com.edward.chat_system.infrastructure.jwt;

import com.edward.chat_system.shared.exception.AppException;
import com.edward.chat_system.shared.exception.ErrorCode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtSigner {
    @Value("${jwt.signerKey}")
    protected String signerKey;

    @Value("${jwt.access-token-duration}")
    protected long accessTokenDuration;

    @Value("${jwt.refresh-token-duration}")
    protected long refreshableDuration;

    @Value("${jwt.tmp-token-duration}")
    protected long tmpTokenDuration;

    public JwtSignerResponse generateToken(JwtClaimObject claim) {

        long duration =
                switch (claim.getTokenType()) {
                    case ACCESS_TOKEN -> accessTokenDuration;
                    case REFRESH_TOKEN -> refreshableDuration;
                    case TMP_TOKEN -> tmpTokenDuration;
                };

        Date expTime = new Date(Instant.now().plus(duration, ChronoUnit.SECONDS).toEpochMilli());
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet =
                new JWTClaimsSet.Builder()
                        .subject(claim.getUserId())
                        .issuer("edward.com")
                        .issueTime(new Date())
                        .expirationTime(expTime)
                        .jwtID(UUID.randomUUID().toString())
                        .claim("tokenType", claim.getTokenType())
                        .claim("username", claim.getUsername())
                        .claim("email", claim.getEmail())
                        // .claim("scope", buildScope(user))
                        .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwt = new JWSObject(header, payload);

        try {
            jwt.sign(new MACSigner(signerKey.getBytes()));
            return JwtSignerResponse.builder()
                    .token(jwt.serialize())
                    .expiresAt(jwtClaimsSet.getExpirationTime())
                    .build();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.JWT_SIGNING_FAILED);
        }
    }
}
