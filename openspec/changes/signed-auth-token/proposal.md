# Proposal

## Why

GitHub Issue [#11](https://github.com/user-bcp-student24/workshop-auth-backend/issues/11), titled `HU-01 - Issue`, covers the signed authentication token. Its committed scope is to ensure token integrity and expiration, and to keep the signing secret externally configurable. These guarantees matter because clients must not accept modified or expired authentication tokens.

## What Changes

### Scope

- Replace the documented unsigned Base64 token value with a signed token whose integrity and expiration can be verified using an externally configured signing secret.
- Require verification to reject altered and expired tokens.
- Preserve the documented `POST /auth/login` route, request behavior, response structure, and established error behavior. The `token` field remains, but its value semantics change as required by the Issue.

### Out of Scope

- Changes to CORS policy, which remains tracked separately under H-04.
- Changes to credential lookup, documented request validation, public user fields, response fields other than the token value, or additional authentication and health endpoints.
- Selection of algorithms, libraries, or internal implementation design.

## Capabilities

### New Capabilities

- `authentication`: login token integrity, expiration, verification, and preservation of the documented login contract.

### Modified Capabilities

- None. The repository has no existing OpenSpec capabilities.

## Impact

`docs/ENDPOINTS.md` is the behavior baseline. Login remains `POST /auth/login`, accepts the documented JSON credentials without prior authentication, and preserves the documented request validation and error responses. The successful JSON response keeps its existing shape: `token`, `expiresIn: 3600`, the public `user` fields (including HTML4-escaped `displayName`), and `debug: "authenticated-with-legacy-flow"`. The Issue intentionally changes the `token` value from the documented unsigned Base64 format to a signed, verifiable token; it does not change the route or response structure.

The updated endpoint document states that the legacy token contains an expiry value but that effective expiry enforcement is not established. Therefore, the Issue's verifiable expiration and rejection of altered or expired tokens are target behavior, not behavior attributed to the baseline. CORS and `GET /q/health` are unchanged and outside this proposal. The document leaves HTTP handling for a request with no body and several framework-level errors unverified; this proposal does not define those as guaranteed HTTP scenarios.