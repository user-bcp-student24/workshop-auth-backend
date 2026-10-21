package com.bcp.workshop.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthTokenServiceTest {
    private static final String TEST_TOKEN_SECRET = "workshop-test-signing-key-do-not-use-outside-tests";
    private static final String OTHER_TOKEN_SECRET = "different-test-signing-key";
    private static final String ISSUER = "workshop-auth-backend";
    private static final String AUDIENCE = "workshop-requirements-api";

    @Inject
    AuthTokenService tokenService;

    @Test
    void verifiesIssuedTokenWithConfiguredTestSecretAndExpectedClaims() {
        String token = tokenService.create("student", "STUDENT");

        DecodedJWT verified = tokenService.verify(token);

        assertEquals(ISSUER, verified.getIssuer());
        assertTrue(verified.getAudience().contains(AUDIENCE));
        assertEquals("student", verified.getSubject());
        assertEquals("STUDENT", verified.getClaim("role").asString());
        assertNotNull(verified.getExpiresAt());
        assertTrue(verified.getExpiresAt().after(new Date()));
    }

    @Test
    void rejectsModifiedSignature() {
        String[] parts = tokenService.create("student", "STUDENT").split("\\.");
        char replacement = parts[2].charAt(0) == 'A' ? 'B' : 'A';
        parts[2] = replacement + parts[2].substring(1);

        assertThrows(JWTVerificationException.class,
                () -> tokenService.verify(String.join(".", parts)));
    }

    @Test
    void rejectsExpiredToken() {
        String token = signedToken(TEST_TOKEN_SECRET, ISSUER, AUDIENCE, Instant.now().minusSeconds(1));

        assertThrows(JWTVerificationException.class, () -> tokenService.verify(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        String token = signedToken(OTHER_TOKEN_SECRET, ISSUER, AUDIENCE, Instant.now().plusSeconds(60));

        assertThrows(JWTVerificationException.class, () -> tokenService.verify(token));
    }

    @Test
    void rejectsUnexpectedIssuerAndAudience() {
        String wrongIssuer = signedToken(TEST_TOKEN_SECRET, "unexpected-issuer", AUDIENCE, Instant.now().plusSeconds(60));
        String wrongAudience = signedToken(TEST_TOKEN_SECRET, ISSUER, "unexpected-audience", Instant.now().plusSeconds(60));

        assertThrows(JWTVerificationException.class, () -> tokenService.verify(wrongIssuer));
        assertThrows(JWTVerificationException.class, () -> tokenService.verify(wrongAudience));
    }

    @Test
    void rejectsBlankSigningSecret() {
        AuthTokenService unconfigured = new AuthTokenService();
        unconfigured.secret = " ";

        IllegalStateException failure = assertThrows(IllegalStateException.class, unconfigured::validateConfiguration);
        assertEquals("AUTH_TOKEN_SECRET is required", failure.getMessage());
    }

    private static String signedToken(String secret, String issuer, String audience, Instant expiresAt) {
        return JWT.create()
                .withIssuer(issuer)
                .withAudience(audience)
                .withSubject("student")
                .withClaim("role", "STUDENT")
                .withExpiresAt(Date.from(expiresAt))
                .sign(Algorithm.HMAC256(secret));
    }
}