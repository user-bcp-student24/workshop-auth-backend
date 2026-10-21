package com.bcp.workshop.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AuthResourceTest {
    private static final String TEST_TOKEN_SECRET = "workshop-test-signing-key-do-not-use-outside-tests";

    @Test
    void logsInWithWorkshopUser() {
        String token = given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"client\":\"angular-workshop\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("user.username", is("student"))
                .body("user.displayName", is("Workshop Student"))
                .body("user.role", is("STUDENT"))
                .body("expiresIn", is(3600))
                .body("debug", is("authenticated-with-legacy-flow"))
                .body("token", notNullValue())
                .extract().path("token");

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_TOKEN_SECRET)).build().verify(token);
        assertEquals("workshop-auth-backend", decoded.getIssuer());
        assertTrue(decoded.getAudience().contains("workshop-requirements-api"));
        assertEquals("student", decoded.getSubject());
        assertEquals("STUDENT", decoded.getClaim("role").asString());
        assertNotNull(decoded.getExpiresAt());
        assertTrue(decoded.getExpiresAt().after(new Date()));
    }

    @Test
    void logsInWithoutClientAndIgnoresUnknownPropertiesWhileEscapingDisplayName() {
        QuarkusMock.installMockForType(new LegacyDatabase() {
            @Override
            public UserRecord findByCredentials(String username, String password) {
                return new UserRecord(1L, username, "<b>QA & Test</b>", "STUDENT");
            }
        }, LegacyDatabase.class);

        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\",\"unexpected\":true}")
                .when().post("/auth/login")
                .then()
                .statusCode(200)
                .body("user.username", is("student"))
                .body("user.displayName", is("&lt;b&gt;QA &amp; Test&lt;/b&gt;"))
                .body("user.role", is("STUDENT"))
                .body("expiresIn", is(3600))
                .body("debug", is("authenticated-with-legacy-flow"))
                .body("token", notNullValue());
    }

            @Test
            void rejectsMissingOrBlankCredentialsWithUsernameValidationFirst() {
            given()
                .contentType("application/json")
                .body("{\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Username is required"));

            given()
                .contentType("application/json")
                .body("{\"username\":\"  \",\"password\":\"  \"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Username is required"));

            given()
                .contentType("application/json")
                .body("{\"username\":null,\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Username is required"));

            given()
                .contentType("application/json")
                .body("{\"username\":\"student\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Password is required"));

            given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"  \"}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Password is required"));

            given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":null}")
                .when().post("/auth/login")
                .then()
                .statusCode(400)
                .body("message", is("Password is required"));
            }

    @Test
    void returnsDocumentedServerErrorFieldsWhenTokenIssuanceFails() {
        QuarkusMock.installMockForType(new AuthTokenService() {
            @Override
            public String create(String username, String role) {
                throw new IllegalStateException("Controlled token failure");
            }
        }, AuthTokenService.class);

        given()
                .contentType("application/json")
                .body("{\"username\":\"student\",\"password\":\"Workshop123!\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(500)
                .body("message", is("Controlled token failure"))
                .body("exception", is("java.lang.IllegalStateException"));
    }

    @Test
    void rejectsNonMatchingCredentials() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"unknown-user\",\"password\":\"not-the-password\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(401)
                .body("message", is("Invalid user unknown-user or password"));
    }

    @Test
    void rejectsCredentialsContainingSqlQuoteCharacters() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"unknown'user\",\"password\":\"not-the-password\"}")
                .when().post("/auth/login")
                .then()
                .statusCode(401)
                .body("message", is("Invalid user unknown'user or password"));
    }
}
