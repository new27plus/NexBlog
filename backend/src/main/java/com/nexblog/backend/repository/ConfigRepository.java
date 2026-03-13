package com.nexblog.backend.repository;

import com.nexblog.backend.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigRepository extends JpaRepository<SystemConfig, Long> {

    Optional<SystemConfig> findFirstByOrderByIdAsc();
}
