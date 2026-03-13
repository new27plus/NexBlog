package com.nexblog.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /*
     * 这里先和你当前 Article 的命名保持一致（createTime / upDateTime），
     * 这样你后续写通用 DTO 或复制 CRUD 模板时不容易混。
     * 等你统一做字段规范时，再一起改成 createdAt / updatedAt。
     */
    private LocalDateTime createTime;

    private LocalDateTime upDateTime;

}
