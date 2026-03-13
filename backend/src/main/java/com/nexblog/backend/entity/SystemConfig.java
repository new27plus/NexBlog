package com.nexblog.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String githubOwner;

    private String githubRepo;

    private String publishBranch;

    private String githubToken;

    private String siteBasePath;
}
