# Tasks

## 1. Characterize the Login Contract Before Token Changes

- [x] 1.1 Add or complete login-success characterization tests for `POST /auth/login`: assert status and documented response fields/values without pinning token bytes; cover optional `client`, ignored JSON properties, and HTML4 escaping with a suitable fixture. Verify with `mvn test -Dtest=AuthResourceTest`.
- [x] 1.2 Characterize documented validation and credential failures: cover missing or blank username/password, validation order, and the exact 400/401 status and `message` behavior. Verify with `mvn test -Dtest=AuthResourceTest`.
- [x] 1.3 Characterize the resource's 500 response using a controlled token-issuance failure in a test; assert status 500 and the `message` and `exception` fields without adding a production endpoint or changing error handling. Verify with `mvn test -Dtest=AuthResourceTest`.

## 2. Complete Token Verification

- [x] 2.1 Complete token verification in the existing authentication service using the current JWT library and externally configured secret; enforce the expected signing algorithm, issuer, audience, and expiration. Verify a newly issued token with the test-profile secret and expected claims using `mvn test -Dtest=AuthResourceTest,AuthTokenServiceTest`.
- [x] 2.2 Add negative verifier tests for a modified token, an expired token, and a token signed with a different secret; verify each is rejected with `mvn test -Dtest=AuthTokenServiceTest`.
- [x] 2.3 Verify signing-secret configuration remains external: test that the configured test secret validates an issued token, blank configuration fails closed, and production code/configuration contains no embedded signing secret. Verify with the focused token/configuration tests and a source review.

## 3. Integrated Verification

- [x] 3.1 Run `mvn test` and `openspec validate "signed-auth-token" --type change --strict --json`; confirm the Issue scenarios pass, the documented login response contract remains unchanged apart from token semantics, and no CORS or unrelated endpoint behavior was added.