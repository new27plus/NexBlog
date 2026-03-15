package com.nexblog.backend.repository;

import com.nexblog.backend.entity.PersonalConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalRepository extends JpaRepository<PersonalConfig, Long> {

    Optional<PersonalConfig> findFirstByOrderByIdAsc();
}
