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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ImprovedHomeControllerTest {

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
                
        // Modify the HomeController to check for existing email before saving
        // This is a suggestion for how the controller should be improved
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Check if a user with this email already exists
            User existingUser = new User();
            when(userRepository.findByEmail(user.getEmail())).thenReturn(
                "duplicate@example.com".equals(user.getEmail()) ? existingUser : null
            );
            
            if (userRepository.findByEmail(user.getEmail()) != null) {
                return null; // Don't save, return null to indicate failure
            }
            
            // If no duplicate, proceed with save
            user.setId(1L); // Simulate setting ID
            return user;
        }).when(userRepository).save(any(User.class));
    }

    @Test
    public void testSignupWithUniqueEmail() throws Exception {
        // Mock the userRepository to return null for findByEmail (no existing user)
        when(userRepository.findByEmail("unique@example.com")).thenReturn(null);
        
        // Mock successful save
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

    @Test
    public void testSignupWithDuplicateEmail() throws Exception {
        // Mock the userRepository to return an existing user for findByEmail
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setEmail("duplicate@example.com");
        when(userRepository.findByEmail("duplicate@example.com")).thenReturn(existingUser);
        
        // Perform POST request to /signup
        mockMvc.perform(post("/signup")
                .param("username", "testuser")
                .param("email", "duplicate@example.com")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Email already exists"))
                .andExpect(view().name("inscription"));
                
        // Verify that save was never called
        verify(userRepository, never()).save(any(User.class));
    }
}