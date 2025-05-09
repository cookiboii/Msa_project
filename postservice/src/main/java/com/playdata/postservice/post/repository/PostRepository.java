package com.playdata.postservice.post.repository;


import com.playdata.postservice.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
