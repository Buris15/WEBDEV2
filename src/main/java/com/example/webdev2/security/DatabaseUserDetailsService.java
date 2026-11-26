package com.example.webdev2.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/** Loads application identities from app_user. These are not MySQL login accounts. */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private static final Set<String> ALLOWED_ROLES = Set.of("MANAGER", "READER");
    private final JdbcTemplate jdbcTemplate;

    public DatabaseUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<AppUserRow> users = jdbcTemplate.query(
                "SELECT username, password_hash, role, enabled FROM app_user WHERE username = ?",
                (rs, rowNumber) -> new AppUserRow(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getBoolean("enabled")),
                username);

        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        AppUserRow account = users.get(0);
        if (!ALLOWED_ROLES.contains(account.role())) {
            throw new UsernameNotFoundException("The account has no supported application role.");
        }

        return User.withUsername(account.username())
                .password(account.passwordHash())
                .roles(account.role())
                .disabled(!account.enabled())
                .build();
    }

    private record AppUserRow(String username, String passwordHash, String role, boolean enabled) { }
}
