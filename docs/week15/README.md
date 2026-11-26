# Week 15 Setup Quick Guide

The actual Maven project root is the folder containing `pom.xml`, `mvnw`, `mvnw.cmd`, and `src/`. In the provided WEBDEV2 archive this folder is named `WEBDEV2`.

1. Run INFOMAN1 `week15/auth_schema.sql` as the local MySQL administrator.
2. Provision the two restricted MySQL accounts using INFOMAN1 `week15/access_control.sql`, after replacing its local password markers in an untracked copy.
3. Generate two BCrypt hashes from this project root:

   ```powershell
   .\mvnw.cmd "-Dspring-boot.run.arguments=--hash-password" spring-boot:run
   ```

4. Insert the hashes using an untracked local copy of INFOMAN1 `week15/seed_test_users.template.sql`.
5. Configure the MySQL runtime account in PowerShell:

   ```powershell
   $env:DB_URL = "jdbc:mysql://127.0.0.1:3307/infoman1_vetclinic_finals?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   $env:DB_USERNAME = "capstone_app"
   $secure = Read-Host "MySQL capstone_app password" -AsSecureString
   $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
   try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
   finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
   $env:SESSION_COOKIE_SECURE = "false"
   ```

6. Run tests and start the application:

   ```powershell
   .\mvnw.cmd test
   .\mvnw.cmd spring-boot:run
   ```

7. Open `http://localhost:8080/` and sign in with the locally provisioned application user.

If PowerShell says `mvnw.cmd` is not recognized, run `Get-ChildItem` and move into the nested `WEBDEV2` folder that contains `pom.xml` and `mvnw.cmd`. Do not run Maven from the parent folder merely named `week14` if the wrapper is one level below.
