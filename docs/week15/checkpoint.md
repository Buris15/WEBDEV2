# WEBDEV2 Week 15 F3 Checkpoint

## Project continuity

- Project: Pet Veterinary Clinic.
- Continue the existing Week 14 application at the repository root.
- Create `week15` from `week14`; keep previous branches unchanged.
- Spring Boot parent: 3.5.5; Java target: 17.
- Database: `infoman1_vetclinic_finals` on local MySQL `127.0.0.1:3307`.
- INFOMAN1 dependency: Week 15 `auth_schema.sql`, `access_control.sql`, and local fictional test-user seed. See the INFOMAN1 repository's `week15/` folder.

## Setup order

1. Create the WEBDEV2 `week15` branch from the submitted `week14` branch.
2. Run INFOMAN1 Week 15 `auth_schema.sql` as the local schema maintainer.
3. Provision MySQL `capstone_app` and `capstone_report` using the sanitized INFOMAN1 template with locally chosen passwords. The WEBDEV2 datasource must use `capstone_app`, not root.
4. Generate two BCrypt hashes with the `--hash-password` tool. Insert fictional `clinic_manager` and `clinic_reader` rows into `app_user` using a local, untracked SQL file.
5. Set `DB_URL`, `DB_USERNAME=capstone_app`, and `DB_PASSWORD` in the same terminal that starts the app. Set `SESSION_COOKIE_SECURE=false` for local HTTP only.
6. Run `mvnw.cmd test`, then `mvnw.cmd spring-boot:run` from the folder that contains `pom.xml` and `mvnw.cmd`.
7. Open `http://localhost:8080/`, sign in as each test role, and execute the cases in `security_tests.md`.

## Environment configuration names

```text
DB_URL=jdbc:mysql://127.0.0.1:3307/infoman1_vetclinic_finals?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=capstone_app
DB_PASSWORD=<local MySQL capstone_app password>
SESSION_COOKIE_SECURE=false
```

The MySQL database password above is separate from each fictional application's login password. Do not commit local values or screenshots of secrets.

## Requirement → feature → test/evidence

| Requirement | Feature / file | Test / evidence |
|---|---|---|
| Adaptive password hashing | `SecurityConfig` BCrypt encoder; `PasswordHashTool`; `app_user.password_hash` | Show BCrypt hash prefix in DB only; never display plaintext or return hash from API |
| Session authentication | `SecurityConfig`, `/api/auth/login`, `/api/auth/me` | Correct and incorrect login; authenticated session and current-user endpoint |
| Logout invalidates session | `/api/auth/logout` | Logout returns 200; subsequent protected GET returns 401 |
| READER can read but cannot mutate | Request matchers and `renderPets`/`renderAppointments` role controls | Reader GET 200; reader POST with valid CSRF returns 403; no row inserted |
| MANAGER can manage records | Request matchers and Week 14 service methods | Manager create/update/delete operations succeed |
| CSRF remains enabled | `HttpSessionCsrfTokenRepository`, `/api/auth/csrf`, JS request helper | Missing-token mutation returns 403; valid-token manager mutation succeeds |
| Frontend does not store password/session ID | Same-origin fetch and HttpOnly `JSESSIONID` | Inspect browser storage: no localStorage session or password values |
| Runtime DB user is restricted | `DB_USERNAME=capstone_app` | DB `CURRENT_USER()` identifies capstone_app; INFOMAN1 DDL-denial test fails with error 1142 |

## Security tests — actual versus expected

See `security_tests.md`. Record the actual HTTP status, UI outcome, role, valid-CSRF state, relevant SQL before/after query, and evidence filename. Keep every row PENDING until executed.

## Known limitations / local validation

- This package contains the application security implementation and setup scripts, but it cannot create accounts on the student's local MySQL server or run a browser session in the student's environment.
- The two fictional application users are not pre-seeded with known passwords. Generate BCrypt hashes locally and insert them through an administrator connection.
- The MySQL database runtime password and each application login password are distinct credentials.
- Local `http://localhost:8080` is for development. Use HTTPS and set the secure session-cookie option for a deployed environment.

## Change log

- Added Spring Security session authentication and JSON login/logout responses.
- Added BCrypt password verification backed by `app_user`.
- Added MANAGER/READER backend authorization and role-aware browser controls.
- Added CSRF bootstrap and token headers for every browser mutation.
- Changed the datasource default username from root to `capstone_app` while retaining `ddl-auto=validate`.
