package com.safety;

import com.safety.config.ContentGuardProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 内容安全检测服务启动入口
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling // 启用定时任务
@EnableConfigurationProperties(ContentGuardProperties.class) // 启用配置类
public class ContentGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentGuardApplication.class, args);
    }
}
