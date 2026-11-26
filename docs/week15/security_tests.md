# WEBDEV2 Week 15 — Security Test Matrix

Run the application from the same origin (`http://localhost:8080`). Create fictional `clinic_manager` and `clinic_reader` accounts first. For all role-denial tests, use a valid login session and a valid CSRF token so the result tests authorization rather than only CSRF.

| ID | Identity / setup | Request or action | Expected | Actual |
|---|---|---|---|---|
| SEC-01 | Anonymous | `GET /api/pets` | 401 | PENDING |
| SEC-02 | Manager, correct password | Sign in through the browser login form | 200; current user shows MANAGER | PENDING |
| SEC-03 | Manager username, incorrect password | Submit login form with wrong password | 401; no session; no protected data | PENDING |
| SEC-04 | Reader, correct password | `GET /api/pets` and `GET /api/appointments` | 200 | PENDING |
| SEC-05 | Reader with valid CSRF | Direct POST `/api/pets` request | 403; no pet row inserted | PENDING |
| SEC-06 | Manager with valid CSRF | Create a disposable pet through the UI | 201; row exists in MySQL | PENDING |
| SEC-07 | Manager with missing CSRF | Direct POST `/api/pets` without `X-CSRF-TOKEN` | 403; no row inserted | PENDING |
| SEC-08 | Manager | Sign out, then GET `/api/pets` | Logout 200, protected GET 401 | PENDING |

## Direct request snippets

### Anonymous protected read

Open DevTools Console before signing in:

```javascript
fetch('/api/pets').then(async response => {
  console.log('HTTP', response.status, await response.text());
});
```

Expected: 401 JSON response.

### Reader mutation with a valid CSRF token

Sign in as `clinic_reader` first. Then run this in the browser console. Use an owner ID that exists in your database; the request should be blocked by the role rule before the controller can insert anything.

```javascript
(async () => {
  const csrf = await fetch('/api/auth/csrf', { credentials: 'same-origin' }).then(r => r.json());
  const response = await fetch('/api/pets', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token
    },
    body: JSON.stringify({
      microchipNumber: 'W15-READER-DENIED-001',
      name: 'Reader Denied Test',
      species: 'Dog',
      breed: 'Test',
      age: 1,
      ownerId: 1001
    })
  });
  console.log('HTTP', response.status, await response.text());
})();
```

Expected: 403. Verify that `W15-READER-DENIED-001` does not exist in `pet`.

### Manager mutation without a CSRF token

Sign in as `clinic_manager`, then run this request without the CSRF header:

```javascript
fetch('/api/pets', {
  method: 'POST',
  credentials: 'same-origin',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    microchipNumber: 'W15-CSRF-DENIED-001',
    name: 'CSRF Denied Test',
    species: 'Dog',
    breed: 'Test',
    age: 1,
    ownerId: 1001
  })
}).then(async response => console.log('HTTP', response.status, await response.text()));
```

Expected: 403 from CSRF protection. This is a separate case from the READER role denial.

## Evidence rules

Capture the browser login result, current username/role, Network request method and status, the role-denied direct request with valid CSRF, the missing-CSRF failure, and the logout followed by 401. Verify denied writes with a MySQL SELECT showing no row. Never capture typed passwords, JSESSIONID cookies, CSRF token values, or database credentials.
