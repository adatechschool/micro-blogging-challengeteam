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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private PostRepository postRepository;

    @Autowired
    public HomeController(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
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
    public String signup(@ModelAttribute("user") User user, HttpSession session) {
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
    public String processLogin(@ModelAttribute("user") User user, Model model,HttpSession session) {
        User existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            // ✅ Connexion réussie (mot de passe en clair ici)
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
