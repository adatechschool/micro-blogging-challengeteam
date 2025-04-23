package com.microblog.app.repositories;

import com.microblog.app.models.Post;
import com.microblog.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserOrderByCreatedAtDesc(User user);

    List<Post> findByUser(User user);
}
