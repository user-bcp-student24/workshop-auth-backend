package com.bcp.workshop.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class AuthTokenService {
    public static final int TOKEN_TTL_SECONDS = 3600;
    private static final String TOKEN_ISSUER = "workshop-auth-backend";
    private static final String TOKEN_AUDIENCE = "workshop-requirements-api";

    @ConfigProperty(name = "auth.token.secret")
    String secret;

    @PostConstruct
    void validateConfiguration() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("AUTH_TOKEN_SECRET is required");
        }
    }

    public String create(String username, String role) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(TOKEN_ISSUER)
                .withAudience(TOKEN_AUDIENCE)
                .withSubject(username)
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(TOKEN_TTL_SECONDS)))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(TOKEN_ISSUER)
                .withAudience(TOKEN_AUDIENCE)
                .build()
                .verify(token);
    }
}