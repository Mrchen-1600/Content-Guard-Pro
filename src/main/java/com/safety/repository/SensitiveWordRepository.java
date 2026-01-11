package com.safety.repository;

import com.safety.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {
    // 查询所有启用的敏感词
    List<SensitiveWord> findByStatus(Integer status);
}