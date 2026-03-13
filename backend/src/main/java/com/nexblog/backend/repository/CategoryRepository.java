package com.nexblog.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexblog.backend.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{

    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
