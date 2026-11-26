# WEBDEV2 Week 15 — F3 Lecture: Authentication and Authorization

## 1. Security boundary and roles

Authentication establishes the identity of the signed-in person. Authorization determines what that identity may do. The Pet Veterinary Clinic uses two application roles stored in the `app_user` table: `MANAGER` and `READER`.

| Action | Anonymous | READER | MANAGER |
|---|---:|---:|---:|
| Open static login page | Allowed | Allowed | Allowed |
| Read pets, appointments, owners, veterinarians, availability, and pet details | Denied | Allowed | Allowed |
| Create or update a pet | Denied | Denied | Allowed |
| Delete a pet | Denied | Denied | Allowed |
| Book, update, or cancel an appointment | Denied | Denied | Allowed |
| Call a protected API directly | Denied | Only permitted GET requests | Permitted reads and mutations |

The frontend hides write forms and mutation buttons from a READER, but this is only usability. Spring Security also checks the role on the backend, so a user cannot gain write access by manually sending an HTTP request.

Application users (`clinic_manager`, `clinic_reader`) are rows in `app_user`. They are not MySQL connection accounts. The server connects to MySQL as `capstone_app`; the browser never receives that database password.

## 2. Password storage and local provisioning

The project uses Spring Security's `BCryptPasswordEncoder` with work factor 12. The browser submits a password only to the same-origin login endpoint over the local development connection; Spring Security verifies it against the BCrypt hash stored in `app_user`. The authentication response contains only the username and role list. It never returns the password or hash.

There is no public registration endpoint. A local administrator creates fictional MANAGER and READER test accounts by generating BCrypt hashes with the one-time `--hash-password` helper and inserting those hashes through an untracked local SQL file. The application runtime account has SELECT on `app_user` only; it cannot create or change application accounts.

Run the hash tool in a real interactive terminal from the folder containing `pom.xml` and `mvnw.cmd`:

```powershell
.\mvnw.cmd "-Dspring-boot.run.arguments=--hash-password" spring-boot:run
```

Enter a strong 12–72 character test password when prompted. Generate one hash for each role. Copy the two hashes into a local copy of INFOMAN1 `week15/seed_test_users.template.sql`; do not commit that local copy. The tool does not write passwords to files or log the plaintext password.

## 3. Session, cookie, CSRF, and CORS choice

This milestone uses Spring Security's server-side HTTP session. The browser receives an HttpOnly `JSESSIONID` cookie; JavaScript does not read or store the session ID. The cookie is configured with `SameSite=Lax`. For local HTTP testing, the `Secure` flag is false; set `SESSION_COOKIE_SECURE=true` when the site is served over HTTPS.

The frontend and API are served from the same Spring Boot origin at `http://localhost:8080`, so the browser sends the session cookie automatically with same-origin requests. The application obtains a CSRF token from `GET /api/auth/csrf` and keeps the token in memory. Every POST, PUT, PATCH, and DELETE request sends it using the configured `X-CSRF-TOKEN` header. Spring Security rotates/clears the token after login and logout, so the frontend fetches a fresh token after each event.

CSRF protects cookie-authenticated browser requests from cross-site request forgery. CORS controls which browser origins may read cross-origin responses; it is not authentication and is not a substitute for CSRF. The recommended test setup is the same-origin Spring Boot page, not Live Server on a different port.

## 4. Backend authorization and observable responses

`SecurityConfig` is the request boundary. It permits the static page, CSRF bootstrap, and login endpoint. Authenticated READER and MANAGER accounts can use GET `/api/**`. Only MANAGER can use POST, PUT, PATCH, and DELETE `/api/**`. The `/api/auth/me` endpoint returns the current username and role names only.

The application returns JSON `401` when a requester is unauthenticated and JSON `403` when a signed-in requester lacks the role or has an invalid/missing CSRF token. For role tests, first establish a valid session and obtain a valid CSRF token; otherwise a CSRF rejection could be mistaken for a role rejection.

Logout is a CSRF-protected POST to `/api/auth/logout`. Spring Security invalidates the server-side session and deletes the session cookie. A later protected request must return 401.

## 5. Configuration and diagnosis

The security dependency matches the project's Spring Boot 3.5.5 parent and Java 17 target. User loading is implemented by `DatabaseUserDetailsService`, which selects `username`, `password_hash`, `role`, and `enabled` from `app_user` using a bound `JdbcTemplate` parameter. The role is checked against the supported `MANAGER` and `READER` values before authorities are created.

A common diagnostic mistake is interpreting every 403 as proof that role authorization works. A missing CSRF token also causes 403. Correct testing therefore includes a valid authenticated session and valid CSRF token for a READER's denied mutation, and a separate test with a missing token. Another common mistake is exposing a database password or session ID in browser storage; this implementation keeps database credentials on the server and the session cookie HttpOnly.

## 6. Project-specific test interpretation

The security matrix is in `security_tests.md`. It covers anonymous access, correct and incorrect login, both roles, direct API denial, permitted manager CRUD, CSRF failure, and logout. Each result must be captured from the local application and database. Do not claim a case passed until the exact request, status, and unchanged database state have been verified.
