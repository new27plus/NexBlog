package com.nexblog.backend.repository;

import com.nexblog.backend.entity.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {

    Optional<AdminAccount> findFirstByOrderByIdAsc();

    Optional<AdminAccount> findByUsername(String username);
}
