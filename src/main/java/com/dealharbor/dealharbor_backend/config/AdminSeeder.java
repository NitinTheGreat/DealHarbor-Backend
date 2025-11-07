package com.dealharbor.dealharbor_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

/**
 * Admin User Seeder - DISABLED
 * 
 * ⚠️ THIS CLASS IS PERMANENTLY DISABLED
 * 
 * Admins should be created via the application's registration flow, then promoted:
 * 
 * Steps:
 * 1. Register user via POST /api/auth/register
 * 2. Verify email with OTP
 * 3. Run SQL: UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@example.com';
 * 
 * See CREATE_ADMIN_USER.sql for detailed instructions.
 * 
 * Security note: Never commit credentials to source code.
 */
@Slf4j
// @Component  // PERMANENTLY DISABLED - Use SQL script instead
public class AdminSeeder implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("AdminSeeder is disabled. Use CREATE_ADMIN_USER.sql to create admin users.");
    }
}
