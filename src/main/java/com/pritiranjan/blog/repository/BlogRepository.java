package com.pritiranjan.blog.repository;

import com.pritiranjan.blog.entity.Blog;
import com.pritiranjan.blog.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    Optional<Blog> findBySlugAndPublishedTrue(String slug);

    Optional<Blog> findBySlug(String slug);

    Page<Blog> findByPublishedTrue(Pageable pageable);

    Page<Blog> findByCategoryAndPublishedTrue(Category category, Pageable pageable);
}

