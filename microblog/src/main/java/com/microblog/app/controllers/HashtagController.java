package com.microblog.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import com.microblog.app.models.Post;
import com.microblog.app.repositories.PostRepository;
import com.microblog.app.utils.HashtagUtil;

@Controller
public class HashtagController {

    private final PostRepository postRepository;

    @Autowired
    public HashtagController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/hashtag/{tag}")
    public String showPostsByHashtag(
            @PathVariable("tag") String tag,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findByDescriptionContainingIgnoreCase(tag, pageable);

        List<Post> posts = postPage.getContent();
        posts.forEach(post -> {
            if (post.getDescription() != null) {
                post.setDescription(HashtagUtil.linkifyHashtags(post.getDescription()));
            }
        });

        model.addAttribute("posts", posts);
        model.addAttribute("tag", tag);
        model.addAttribute("currentPage", page);

        return (page == 0) ? "hashtag-posts" : "fragments/post-list :: postList";
    }

}