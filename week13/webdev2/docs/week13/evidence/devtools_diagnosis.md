# Week 13 — DevTools and Connection Diagnosis

**Problem encountered:**

During the initial Spring Boot startup, the application reported `Unknown database 'infoman1_vetclinic_finals'`.

**Investigation:**

I checked the running database services and discovered that XAMPP MariaDB was listening on port 3306, while MySQL Community Server 8.0.46 was available on port 3307.

The database schema was created on the actual MySQL server, so the application needed to connect to port 3307.

**Solution:**

I corrected the Spring Boot database URL to use port 3307 and kept the database credentials in local environment variables.

**Verification:**

I tested the application endpoints in the browser and checked the API responses through Chrome Developer Tools.

**Lesson learned:**

A database connection error can happen when multiple database servers are running on the same computer. Checking the server version, port, and database name helps identify the correct connection.