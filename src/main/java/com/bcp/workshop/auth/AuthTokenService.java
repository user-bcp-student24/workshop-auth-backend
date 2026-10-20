package com.bcp.workshop.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.Date;

@ApplicationScoped
public class AuthTokenService {
    public static final int TOKEN_TTL_SECONDS = 3600;

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
            .withIssuer("workshop-auth-backend")
            .withAudience("workshop-requirements-api")
                .withSubject(username)
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(TOKEN_TTL_SECONDS)))
                .sign(Algorithm.HMAC256(secret));
    }
}