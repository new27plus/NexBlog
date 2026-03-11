package com.nexblog.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexblog.backend.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    /*
     * 第一个功能先用到的查询：
     * 1) keyword 为空 -> findAll(pageable)
     * 2) keyword 不为空 -> 按 title 模糊查询
     */
    Page<Article> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
