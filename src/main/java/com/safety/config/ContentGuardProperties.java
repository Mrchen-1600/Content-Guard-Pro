package com.safety.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 统一配置映射类
 * 对应 application.yml 中的 content-guard 前缀
 */
@Data
@ConfigurationProperties(prefix = "content-guard")
public class ContentGuardProperties {

    private AsyncPool async = new AsyncPool();
    private TextConfig text = new TextConfig();
    private SecurityConfig security = new SecurityConfig();
    private LlmConfig llm = new LlmConfig();

    @Data
    public static class AsyncPool {
        private PoolConfig cpu = new PoolConfig();
        private PoolConfig io = new PoolConfig();
    }

    @Data
    public static class PoolConfig {
        private int corePoolSize; // 核心线程数
        private int maxPoolSize; // 最大线程数
        private int queueCapacity; // 队列容量
        private String prefix; // 线程名称前缀
    }

    @Data
    public static class TextConfig {
        private int premiumThreshold; // 优质用户全文检测阈值
        private int chunkSize; // 文本分片大小
        private double overlapRatio; // 分片重叠比例
    }

    @Data
    public static class SecurityConfig {
        private long acRefreshRateMs; // AC 自动机词库刷新间隔
        private List<String> injectionKeywords; // 防止Prompt注入的关键词
    }

    @Data
    public static class LlmConfig {
        private String apiUrl; // LLM API 地址
        private String model; // LLM 模型名称
        private double temperature; // LLM 模型温度
        private int connectTimeoutSeconds; // LLM API 连接超时时间
        private int readTimeoutSeconds; // LLM API 读取超时时间
        private List<String> apiKeys; // LLM API 密钥
        private RateLimit rateLimit = new RateLimit(); // LLM API 速率限制
    }

    @Data
    public static class RateLimit {
        private String key; // 速率限制的 Redis key
        private int permitsPerSecond; // 每秒允许的请求数
        private int expireSeconds; // 速率限制的过期时间
    }
}