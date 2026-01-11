package com.safety.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AsyncConfig {

    private final ContentGuardProperties properties;

    /**
     * 配置CPU密集型任务线程池
     */
    @Bean(name = "cpuExecutor")
    public Executor cpuExecutor() {
        ContentGuardProperties.PoolConfig config = properties.getAsync().getCpu();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setThreadNamePrefix(config.getPrefix());
        executor.initialize();
        log.info("CPU线程池已初始化: core={}, max={}", config.getCorePoolSize(), config.getMaxPoolSize());
        return executor;
    }


    /**
     * 配置IO密集型任务线程池
     */
    @Bean(name = "ioExecutor")
    public Executor ioExecutor() {
        ContentGuardProperties.PoolConfig config = properties.getAsync().getIo();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setThreadNamePrefix(config.getPrefix());
        executor.initialize();
        log.info("IO线程池已初始化: core={}, max={}", config.getCorePoolSize(), config.getMaxPoolSize());
        return executor;
    }
}