package com.nexblog.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "personal_config")
public class PersonalConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "sections_json")
    private String sectionsJson;

    private LocalDateTime createTime;

    private LocalDateTime upDateTime;
}
