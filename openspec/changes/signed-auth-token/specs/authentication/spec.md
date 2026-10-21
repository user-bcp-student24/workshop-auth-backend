# Spec Delta

## Purpose

Defines the observable authentication behavior for issuing and verifying tokens while preserving the documented login contract.

## ADDED Requirements

### Requirement: Successful login preserves the documented contract
The system SHALL preserve the documented `POST /auth/login` route, request behavior, and successful JSON response contract.

#### Scenario: Login succeeds with valid credentials
- **WHEN** a client submits valid `username` and `password` values to `POST /auth/login`, with or without the optional `client` field
- **THEN** the system returns `200 OK` with JSON fields `token`, `expiresIn` equal to `3600`, `user.username`, `user.displayName`, `user.role`, and `debug` equal to `authenticated-with-legacy-flow`

#### Scenario: Login accepts unrecognized request properties
- **WHEN** a client submits valid credentials with an unrecognized JSON property
- **THEN** the system processes the request under the existing login contract

### Requirement: Login errors preserve the documented responses
The system SHALL preserve the documented status and response body shape for invalid login requests, unmatched credentials, and server errors.

#### Scenario: Required credentials are missing or blank
- **WHEN** the request is null or `username` or `password` is null or blank
- **THEN** the system returns `400 Bad Request` with a JSON `message` field

#### Scenario: Credentials do not match
- **WHEN** the submitted credentials do not match a user
- **THEN** the system returns `401 Unauthorized` with a JSON `message` describing the invalid username or password

#### Scenario: An exception occurs during login
- **WHEN** an exception occurs while processing a login request
- **THEN** the system returns `500 Internal Server Error` with JSON `message` and `exception` fields

### Requirement: Issued tokens have verifiable signatures and expiration
The system SHALL issue tokens whose signatures can be verified using the externally configured signing secret and whose expiration is enforced by token verification.

#### Scenario: A successfully issued token verifies with the external test secret
- **WHEN** valid credentials produce a token under external test configuration
- **THEN** the token verifier accepts the token using that configured test secret and the token is not expired

### Requirement: Token verification rejects altered and expired tokens
The system SHALL reject a token when its contents have been altered or its expiration has passed.

#### Scenario: An altered token is verified
- **WHEN** a token's contents are modified after issuance and verification is attempted
- **THEN** the token verifier rejects the altered token

#### Scenario: An expired token is verified
- **WHEN** verification is attempted after an issued token has expired
- **THEN** the token verifier rejects the expired token

### Requirement: Production signing secrets come from external configuration
The system SHALL obtain its production signing secret from external configuration and SHALL NOT rely on a signing secret embedded in production code.

#### Scenario: A token is issued with an externally configured secret
- **WHEN** valid credentials are processed with a signing secret supplied through external test configuration
- **THEN** the emitted token verifies with that configured value