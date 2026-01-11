package com.safety.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sensitive_words")
public class SensitiveWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String word;

    /**
     * 敏感词类型，HIGH_RISK, AMBIGUOUS
     */
    private String type;

    /**
     * 状态：1=Enabled, 0=Disabled
     */
    private Integer status;
}