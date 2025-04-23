package com.microblog.app.controllers;

import com.microblog.app.repositories.UserRepository;
import com.microblog.app.models.User;
import com.microblog.app.repositories.PostRepository;
import com.microblog.app.models.Post;
import com.microblog.app.utils.HashtagUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.sql.Timestamp;

@Controller
public class ProfileController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProfileController(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/profile/{user_id}")
    public String showProfile(@PathVariable("user_id") Long user_id, Model model) {
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        List<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(user);

        // Transform hashtags into links
        for (Post post : posts) {
            if (post.getDescription() != null) {
                post.setDescription(HashtagUtil.linkifyHashtags(post.getDescription()));
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        return "profile";
    }

    @GetMapping("/posts/new")
    public String showPostForm(@RequestParam("user_id") Long user_id, Model model) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new RuntimeException("User not found!"));
        Post post = new Post();
        post.setUser(user);
        model.addAttribute("post", post);
        return "new-post";
    }

    @PostMapping("/posts/new")
    public String createPost(@ModelAttribute Post post,
                             @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        if (!imageFile.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            String uploadDir = "src/main/resources/static/uploads/";
            Files.createDirectories(Paths.get(uploadDir));
            Path filePath = Paths.get(uploadDir + filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            post.setImage("/uploads/" + filename);
        }
        post.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        postRepository.save(post);
        return "redirect:/profile/" + post.getUser().getId();
    }

    @PostMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Post post = postRepository.findById(id).orElseThrow();

        /*if (!post.getUser().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "You’re not allowed to delete this post.");
            return "redirect:/profile/" + post.getUser().getId();
        }*/

        postRepository.delete(post);
        redirectAttributes.addFlashAttribute("success", "Post deleted successfully.");
        return "redirect:/profile/" + post.getUser().getId();
    }

    @GetMapping("/profile/edit")
    public String showEditForm(@RequestParam("user_id") Long userId, Model model) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "edit-profile";
    }

    @PostMapping("/users/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                @RequestParam("avatarFile") MultipartFile avatarFile) throws IOException {

        User user = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        user.setFullname(updatedUser.getFullname());
        user.setUsername(updatedUser.getUsername());
        user.setBio(updatedUser.getBio());
        user.setDob(updatedUser.getDob());

        // Handle avatar upload
        if (!avatarFile.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + avatarFile.getOriginalFilename();
            String uploadDir = "src/main/resources/static/pfp"; // Save location in project
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = avatarFile.getInputStream()) {
                Path filePath = uploadPath.resolve(filename);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                user.setAvatar("/pfp/" + filename); // relative path for <img th:src="@{${user.avatar}}">
            } catch (IOException e) {
                throw new IOException("Could not save uploaded file: " + filename, e);
            }
        }

        userRepository.save(user);
        return "redirect:/profile/" + user.getId();
    }
}
