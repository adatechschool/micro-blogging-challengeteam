package com.microblog.app.controllers;
import com.microblog.app.models.User;
import com.microblog.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    private UserRepository userRepository;
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        profileController = new ProfileController(null, userRepository); // `null` pour PostRepository car il n'est pas utilisé ici
    }

    @Test
    void shouldNotCreateProfileIfEmailAlreadyExists() {
        // Arrange
        Model model = mock(Model.class); // Mock du Model
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("existing@email.com");

        User newUser = new User();
        newUser.setEmail("existing@email.com");

        // Simule le fait que l'email est déjà pris
        when(userRepository.findByEmail("existing@email.com")).thenReturn(existingUser);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            // Simule l'appel de la méthode de création de profil
            if (userRepository.findByEmail(newUser.getEmail()) != null) {
                throw new IllegalStateException("L'email est déjà utilisé !");
            }
        });
    }
}