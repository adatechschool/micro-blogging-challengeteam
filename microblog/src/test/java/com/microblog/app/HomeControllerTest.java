package com.microblog.app;

import com.microblog.app.controllers.HomeController;
import com.microblog.app.models.User;
import com.microblog.app.repositories.PostRepository;
import com.microblog.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HomeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    public void testSignupWithDuplicateEmail() throws Exception {
        // Mock the userRepository to throw DataIntegrityViolationException when saving a user with duplicate email
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate email"));

        // Perform POST request to /signup and expect an exception
        try {
            mockMvc.perform(post("/signup")
                    .param("username", "testuser")
                    .param("email", "duplicate@example.com")
                    .param("password", "password123"));
        } catch (Exception e) {
            // Verify that the exception is caused by DataIntegrityViolationException
            Throwable rootCause = e;
            while (rootCause.getCause() != null && !(rootCause instanceof DataIntegrityViolationException)) {
                rootCause = rootCause.getCause();
            }
            assertTrue(rootCause instanceof DataIntegrityViolationException, 
                    "Expected DataIntegrityViolationException, but got: " + rootCause.getClass().getName());
            assertEquals("Duplicate email", rootCause.getMessage());
            return;
        }
        fail("Expected exception was not thrown");
    }

    @Test
    public void testSignupWithUniqueEmail() throws Exception {
        // Mock the userRepository to return a saved user
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("unique@example.com");
        savedUser.setPassword("password123");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Perform POST request to /signup
        mockMvc.perform(post("/signup")
                .param("username", "testuser")
                .param("email", "unique@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/connexion"));
    }
}
