# WEBDEV2 Week 15 Evidence

Capture real results from your local application. Never include passwords, BCrypt hashes, JSESSIONID cookies, or CSRF token values.

Suggested screenshots:

1. `01_anonymous_401.png` — DevTools console/network shows protected GET denied with 401.
2. `02_manager_login.png` — authenticated session shows clinic_manager / MANAGER (hide password field).
3. `03_invalid_login.png` — invalid password shows generic sign-in failure.
4. `04_reader_read.png` — Reader can view pets and appointments.
5. `05_reader_write_denied.png` — direct reader POST with valid CSRF returns 403 and SQL shows zero inserted test rows.
6. `06_manager_crud_allowed.png` — manager creates a disposable test pet and receives 201.
7. `07_missing_csrf_denied.png` — manager mutation without CSRF returns 403 and no row exists.
8. `08_logout_session_invalidated.png` — logout succeeds and subsequent protected read returns 401.
9. `09_database_runtime_identity.png` — MySQL `CURRENT_USER()` confirms `capstone_app` rather than root.

Use exact HTTP statuses. A 403 without a valid CSRF token does not prove the READER role is enforced.
