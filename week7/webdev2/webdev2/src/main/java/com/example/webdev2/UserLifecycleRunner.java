package com.example.webdev2;

import com.example.webdev2.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class UserLifecycleRunner implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. Create User (TRANSIENT)
        User user = new User();

        user.setUsername("student01");
        user.setEmail("original@example.com");
        user.setCreatedAt(LocalDateTime.now());

        System.out.println("STEP 1: User is TRANSIENT");

        // 2. Persist User (MANAGED)
        entityManager.persist(user);
        entityManager.flush();

        System.out.println("STEP 2: User is MANAGED");
        System.out.println("Is managed: " + entityManager.contains(user));

        // 3. Detach User (DETACHED)
        entityManager.detach(user);

        System.out.println("STEP 3: User is DETACHED");
        System.out.println("Is managed: " + entityManager.contains(user));

        // 4. Modify email after detaching
        user.setEmail("changed@example.com");

        System.out.println("STEP 4: Email changed to: " + user.getEmail());

        // 5. Do not merge
        System.out.println("STEP 5: User was NOT merged.");

    }
}
