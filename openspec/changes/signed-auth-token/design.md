# Design

## Context

See [proposal.md](proposal.md) for motivation and [specs/authentication/spec.md](specs/authentication/spec.md) for required behavior.

In the current checkout, `AuthResource` handles `POST /auth/login`, delegates credential lookup to `LegacyDatabase`, and delegates token issuance to `AuthTokenService`. `AuthTokenService` already uses the `java-jwt` dependency (4.5.0) to issue HMAC256 JWTs with issuer, audience, subject, role, issued-at, and expiration claims. The TTL is 3600 seconds. The signing secret is supplied through `auth.token.secret`, mapped from `AUTH_TOKEN_SECRET`, and an empty value fails application startup. The test profile supplies a separate test secret.

The current login test verifies the emitted signature and selected claims, and checks the response fields and TTL. There is no token-verification consumer or endpoint in `src`; verification currently occurs only in tests. There are no tests for tampered or expired tokens. The latest `docs/ENDPOINTS.md` describes an unsigned Base64 token and references source files not present in this checkout. Treat that document as the protected HTTP contract, but treat the current source and tests as evidence of the implementation state. Characterize both before changing security behavior; do not regress already-present signed issuance.

## Goals / Non-Goals

**Goals:**

- Complete the token verification behavior in the existing authentication boundary using the repository's current signing configuration and token library.
- Keep token lifetime, signing, and verification policy consistent, including the expected issuer and audience.
- Preserve the `POST /auth/login` response structure and all documented behavior except the token value's explicitly changed semantics.
- Make the characterization and token-security tests distinguish the HTTP contract from token-format details.

**Non-Goals:**

- Add a login or token-verification endpoint, bearer middleware, or protected business routes.
- Change credential lookup, password hashing, SQLite behavior, CORS, health checks, or the login response fields.
- Add key rotation, multi-key verification, persisted sessions, or legacy unsigned-token acceptance.
- Replace the existing JWT library or redesign authentication across the application.

## Protected Contracts

- Keep `POST /auth/login`, its JSON request semantics, and the response's field names and shape.
- Keep `expiresIn` at 3600, the public `user` fields and their existing values/escaping behavior, and `debug` equal to `authenticated-with-legacy-flow`.
- Keep the documented 400, 401, and 500 status/body behavior. Do not promote the documented request-without-body or framework parsing cases to guaranteed HTTP behavior without characterization; the current endpoint document marks them unverified.
- The `token` remains a string response field, but its bytes/format intentionally change from the documented legacy Base64 value to a signed token as required by the Issue. Clients must treat it as opaque; JSON property ordering is not a contract.

## Components Affected

- `AuthTokenService`: remains the single issuer and owns the corresponding verification policy; use the existing signing secret, algorithm, issuer, audience, and TTL consistently.
- `AuthResource`: retains route, validation, credential lookup, response mapping, and error mapping. No new public route or response field is introduced.
- `application.properties` and the test profile: retain external secret configuration and test-only secret injection; production must not gain an inline fallback secret.
- `AuthResourceTest`: add contract characterization and positive, altered-token, expired-token, and external-secret verification coverage.
- `pom.xml`: no dependency change is expected because `java-jwt` is already present.

## Decisions

### Reuse the current JWT library and signing policy

Use the existing `java-jwt` dependency and HMAC256 signing policy rather than hand-rolling a signed Base64 format or introducing another token library. The current issuer already produces a standard JWT and tests verify it with the configured test key. Verification must pin the expected algorithm, issuer, and audience and rely on the JWT verifier's expiration check; signature validity alone is insufficient.

**Alternatives considered:** A custom Base64 payload plus a home-grown MAC would preserve more of the legacy appearance but duplicates parsing and validation rules. Replacing `java-jwt` adds migration surface without an issue requirement or a demonstrated library gap.

### Keep verification in the existing token service; do not add an endpoint

Keep issuance and verification policy together in `AuthTokenService`, so both use the same external key and token constraints. Exercise verification through focused tests. The repository has no current runtime token consumer, and the Issue does not request a public verification API or protected route; adding either would expand the contract.

**Alternatives considered:** A new HTTP verification endpoint or global bearer filter would add externally visible behavior and affect routes outside this change. A separate token service would duplicate policy without an existing second consumer.

### Keep the secret external and fail closed

Continue using `AUTH_TOKEN_SECRET` through the existing configuration key. Keep the current fail-fast behavior when the value is blank, and use only test-profile configuration for test keys. Do not add a hardcoded development or production fallback.

**Alternatives considered:** An inline fallback makes local startup easier but risks silently issuing tokens under a known or shared key. Secret rotation and multiple active keys are not required for this change.

### Do not accept legacy unsigned tokens

Verification must reject tokens that lack a valid signature or have expired. Do not introduce a compatibility branch that accepts the old unsigned Base64 format; it would defeat the integrity requirement. The response shape remains compatible, but token consumers that decode or depend on the legacy format need coordinated migration.

## Incremental Strategy and Characterization Tests

1. Before changing token behavior, establish HTTP characterization tests from the updated endpoint document: successful status and response fields/values (except exact token bytes), optional and ignored request properties, validation order and documented 400/401/500 responses. Retain the current success and invalid-credential tests. Do not assert a particular HTTP result for an absent body or uncharacterized framework parsing errors.
2. Confirm the current implementation's signed-token behavior in tests before modifying the security boundary: configured test secret, expected issuer/audience/subject/role, and the 3600-second expiration claim. This guards against replacing behavior that is already present in this checkout.
3. Add focused verifier tests for a valid emitted token, a modified token, an expired token, and verification using the externally configured test secret. Keep test secrets out of production configuration and output.
4. Make only the service-level verification change needed to satisfy those tests; leave the resource route and response mapping unchanged. Re-run the contract and token tests together, then the project test suite.
5. Before deployment, provision `AUTH_TOKEN_SECRET` through the environment's secret mechanism and verify startup fails when it is absent. Coordinate token consumers because token contents change even though the HTTP response shape does not.

## Risks / Trade-offs

- **Documentation/source mismatch** -> Characterize the running branch before functional changes; preserve the documented HTTP contract while using the current source as implementation evidence. Do not silently edit the user's endpoint document as part of this design.
- **Consumers depend on legacy Base64 contents** -> Treat token as opaque going forward, coordinate consumers, and require re-authentication where old tokens cannot be verified. Do not weaken verification with a legacy fallback.
- **Different or missing signing secrets cause verification failures or startup failure** -> Provision a stable secret per environment before deployment, verify the configuration in a smoke check, and never log the value.
- **A service-level verifier does not secure routes that do not invoke it** -> Keep this change limited to the Issue's token verification behavior; do not claim that existing application routes gain bearer authorization.
- **Expiration depends on verifier use and clock behavior** -> Test expiration through the same verifier policy used for valid tokens; avoid treating the `expiresIn` response value alone as enforcement.

## Migration Plan and Rollback

No database migration is required; tokens are stateless. Deploy only after `AUTH_TOKEN_SECRET` is provisioned in the target environment and contract/security tests pass. Roll out consumers that rely on the new signed token format in coordination with the issuer, while keeping the login response shape stable.

If rollback is required, roll back the issuer and its consumers as a coordinated release and require clients to obtain fresh tokens. Do not make the rolled-back version accept unsigned tokens as a fallback to tokens from the newer version. Retain secret provisioning until all rolled-back components are running; no key rotation or token migration is included here.