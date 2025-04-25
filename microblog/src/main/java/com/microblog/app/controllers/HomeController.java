package com.microblog.app.controllers;

import org.springframework.data.domain.Sort;
import com.microblog.app.models.Post;
import com.microblog.app.repositories.PostRepository;
import com.microblog.app.utils.HashtagUtil;
import jakarta.servlet.http.HttpSession;
import com.microblog.app.models.User;
import com.microblog.app.repositories.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public HomeController(UserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        posts.forEach(post -> {
            if (post.getDescription() != null) {
                post.setDescription(HashtagUtil.linkifyHashtags(post.getDescription()));
            }
        });
        model.addAttribute("message", "Hello Ada Tech 🚀");
        model.addAttribute("posts", posts);

        return "home";
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("user", users);
        return "users";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("user", new User());
        return "inscription";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("user") User user, HttpSession session, Model model) {
        // Validate required fields
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Username is required");
            return "inscription";
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Email is required");
            return "inscription";
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Password is required");
            return "inscription";
        }

        // Validate email format
        if (!user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            model.addAttribute("error", "Please enter a valid email address");
            return "inscription";
        }

        // Validate password strength (minimum 8 characters)
        if (user.getPassword().length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long");
            return "inscription";
        }

        // Check if email already exists
        User existingUserEmail = userRepository.findByEmail(user.getEmail());
        if (existingUserEmail != null) {
            model.addAttribute("error", "Email already exists");
            return "inscription";
        }

        // Check if username already exists
        User existingUserName = userRepository.findByUsername(user.getUsername());
        if (existingUserName != null) {
            model.addAttribute("error", "Username already exists");
            return "inscription";
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        session.setAttribute("user", user);  // Stocke nouvel utilisateur en session
        return "redirect:/connexion";
    }

    @GetMapping("/connexion")
    public String showLoginForm(Model model){
        model.addAttribute("user", new User());
        return "connexion";
    }

    @PostMapping("/connexion")
    public String processLogin(@ModelAttribute("user") User user, Model model, HttpSession session) {
        User existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser != null && passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            // ✅ Connexion réussie (avec vérification sécurisée du mot de passe)
            session.setAttribute("user", existingUser); // Stocke en session
            return "redirect:/"; // page d’accueil après connexion
        }

        // ❌ Email ou mot de passe incorrect
        model.addAttribute("error", "Identifiants incorrects");
        return "connexion";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
