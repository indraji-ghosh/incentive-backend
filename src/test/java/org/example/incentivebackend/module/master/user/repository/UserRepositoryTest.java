package org.example.incentivebackend.module.master.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void save_ShouldPersistUser() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("Test@123"));
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);

        UserEntity saved = userRepository.save(user);

        assertNotNull(saved.getUserId());
        assertEquals("testuser", saved.getUsername());
        assertTrue(passwordEncoder.matches("Test@123", saved.getPassword()));
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        UserEntity user = new UserEntity();
        user.setUsername("admin1");
        user.setPassword(passwordEncoder.encode("Admin@123"));
        user.setFullName("Admin One");
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByUsername("admin1");

        assertTrue(found.isPresent());
        assertEquals("admin1", found.get().getUsername());
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenNotExists() {
        Optional<UserEntity> found = userRepository.findByUsername("missinguser");
        assertFalse(found.isPresent());
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenExists() {
        UserEntity user = new UserEntity();
        user.setUsername("manager1");
        user.setPassword("hash");
        user.setFullName("Manager");
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("manager1"));
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenNotExists() {
        assertFalse(userRepository.existsByUsername("missingmanager"));
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        UserEntity user = new UserEntity();
        user.setUsername("emailuser");
        user.setPassword("hash");
        user.setFullName("Email User");
        user.setEmail("emailuser@example.com");
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("emailuser@example.com"));
    }

    @Test
    void save_ShouldThrowException_WhenDuplicateUsername() {
        UserEntity user1 = new UserEntity();
        user1.setUsername("duplicate");
        user1.setPassword("hash1");
        user1.setFullName("User 1");
        user1.setStatus(UserStatus.ACTIVE);
        userRepository.saveAndFlush(user1);

        UserEntity user2 = new UserEntity();
        user2.setUsername("duplicate"); // Duplicate
        user2.setPassword("hash2");
        user2.setFullName("User 2");
        user2.setStatus(UserStatus.ACTIVE);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }
}
