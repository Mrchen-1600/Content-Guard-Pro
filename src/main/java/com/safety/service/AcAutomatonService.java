package com.safety.service;

import com.safety.entity.SensitiveWord;
import com.safety.repository.SensitiveWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AcAutomatonService {

    private final SensitiveWordRepository repository; // 敏感词库

    private volatile Trie highRiskTrie; // 高危敏感词
    private volatile Trie ambiguousTrie; // 歧义词

    @PostConstruct
    public void init() {
        refreshDictionary();
    }

    /**
     * 定时刷新词库 (每5分钟执行一次)
     * 解决问题：运营人员后台添加新词后，服务能自动感知
     */
    @Scheduled(fixedDelayString = "${content-guard.security.ac-refresh-rate-ms}")
    public void refreshDictionary() {
        log.info("开始定时刷新敏感词库...");
        try {
            List<SensitiveWord> allWords = repository.findByStatus(1);

            List<String> highRiskKeywords = allWords.stream()
                    .filter(w -> "HIGH_RISK".equals(w.getType()))
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());

            List<String> ambiguousKeywords = allWords.stream()
                    .filter(w -> "AMBIGUOUS".equals(w.getType()))
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());

            if (!highRiskKeywords.isEmpty()) {
                this.highRiskTrie = Trie.builder().addKeywords(highRiskKeywords).build();
            } else {
                this.highRiskTrie = Trie.builder().addKeyword("EMPTY_HOLDER").build();
            }

            if (!ambiguousKeywords.isEmpty()) {
                this.ambiguousTrie = Trie.builder().addKeywords(ambiguousKeywords).build();
            } else {
                this.ambiguousTrie = Trie.builder().addKeyword("EMPTY_HOLDER").build();
            }

            log.info("词库刷新完成. 高风险词: {}, 歧义词: {}", highRiskKeywords.size(), ambiguousKeywords.size());
        } catch (Exception e) {
            log.error("词库刷新失败", e);
        }
    }

    /**
     * 检查高风险敏感词
     */
    public Collection<Emit> checkHighRisk(String text) {
        if (highRiskTrie == null) return List.of();
        return highRiskTrie.parseText(text);
    }

    /**
     * 检查歧义词
     */
    public Collection<Emit> checkAmbiguous(String text) {
        if (ambiguousTrie == null) return List.of();
        return ambiguousTrie.parseText(text);
    }
}