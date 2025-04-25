package com.microblog.app;

import com.microblog.app.models.User;
import com.microblog.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserCreationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateUserWithUniqueEmail() {
        // Create a new user with a unique username (using timestamp to ensure uniqueness)
        String uniqueUsername = "testuser" + System.currentTimeMillis();
        User user = new User();
        user.setUsername(uniqueUsername);
        user.setEmail("test" + System.currentTimeMillis() + "@example.com");
        user.setPassword("password123");

        // Save the user
        User savedUser = userRepository.save(user);

        // Verify the user was saved
        assertNotNull(savedUser.getId());
        assertEquals(user.getEmail(), savedUser.getEmail());
    }

    @Test
    public void testCreateUserWithDuplicateEmail() {
        // Create and save first user with a unique username (using timestamp to ensure uniqueness)
        String uniqueUsername1 = "user1" + System.currentTimeMillis();
        String uniqueEmail = "duplicate" + System.currentTimeMillis() + "@example.com";

        User user1 = new User();
        user1.setUsername(uniqueUsername1);
        user1.setEmail(uniqueEmail);
        user1.setPassword("password123");
        userRepository.save(user1);

        // Create second user with same email but different username
        String uniqueUsername2 = "user2" + System.currentTimeMillis();
        User user2 = new User();
        user2.setUsername(uniqueUsername2);
        user2.setEmail(uniqueEmail); // Same email as user1
        user2.setPassword("password456");

        // Attempt to save second user should throw exception
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(user2);
            userRepository.flush(); // Force the persistence context to flush
        });
    }
}
