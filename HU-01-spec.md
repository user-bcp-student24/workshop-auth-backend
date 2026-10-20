# Summary

Replace the Base64-only authentication token with a signed token whose
expiration is enforced and whose signing secret comes from external
configuration, while preserving the existing `POST /auth/login` contract.
The story covers H-03 and only the token-secret portion of H-04.

## Scope

- **In:** signed-token issuance and verification; rejection of altered or
  expired tokens; external secret configuration; tests that verify these
  behaviors and preserve the login contract.
- **Out:** CORS changes. The CORS portion of H-04 remains outside this story.

## Assumptions

- The existing `POST /auth/login` route and response structure must remain
  unchanged.
- Configuration tests use a test value without revealing it.
- The story does not specify the token format, signing algorithm, claims,
  expiration duration, or verification location; these remain open decisions.

## Technical decisions to make

- Select the signed-token format and signing algorithm.
- Define the claims included in the token.
- Decide how the signing secret is supplied through external configuration
  and what happens if it is missing.
- Define the expiration duration and where token verification occurs.
- Confirm the exact login response structure that must remain unchanged.

## Risks and mitigations

- **Altered token content is accepted:** test that a verifier accepts a valid
  token and rejects one whose content was modified.
- **Expired tokens remain usable:** test that the verifier rejects an expired
  token.
- **Signing secret remains embedded or is exposed:** verify that production
  code has no embedded signing secret and keep test configuration values
  undisclosed.
- **Login contract changes unintentionally:** run characterization tests
  before and after implementation and compare the route and response structure.

## Acceptance criteria

- A valid-login test confirms that a token is returned and its signature is
  accepted using external test configuration.
- A test that modifies an issued token's content confirms that verification
  rejects it.
- A test with an expired token confirms that verification rejects it.
- A configuration test confirms use of the external test setting, and code
  review confirms there is no signing secret embedded in production code.
- Contract tests confirm that the `POST /auth/login` route and agreed response
  structure remain unchanged.

## Validation plan

- **Unit tests:** cover valid signature verification, modified-content
  rejection, and expired-token rejection.
- **Functional tests:** exercise valid login and verify the existing endpoint
  contract.
- **Security checks:** inspect that signature and expiration are enforced, and
  confirm the signing secret is neither embedded in production code nor
  disclosed in test evidence.
- **Evidence required:** automated test results, configuration test results
  without the test value, login contract characterization results, and code
  review confirming no embedded signing secret.

## Suggested tasks by role

- **Chapter/PO:** confirm the login response structure that must be preserved;
  review the acceptance criteria; clarify any product-level constraints on
  token use.
- **Tech Lead:** choose the token format and signing algorithm; define required
  claims; decide expiration duration and verification location; define missing
  configuration behavior.
- **Backend:** implement signed-token issuance and verification; read the
  signing secret from external configuration; enforce expiration; preserve the
  login contract and add the specified tests.
- **Frontend:** no frontend change is specified; confirm whether the client
  depends on token format, and raise any compatibility constraint for the
  technical decision.
- **QA:** test valid token verification; test rejection after content
  modification; test expiration rejection; verify external configuration and
  login contract.
- **Cyber:** review signature and expiration enforcement; check production
  code for embedded signing secrets; ensure validation evidence does not expose
  sensitive values.

## Open questions

- Which signing algorithm and token format will be used?
- Which claims must the token contain?
- How is the signing secret supplied, and what should happen if it is missing?
- What is the expiration duration, and where will verification occur?
- What exact response fields make up the login contract to preserve?
- Are there product-level constraints on token use that the story does not
  specify?
