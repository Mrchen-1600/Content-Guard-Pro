# 🛡️ ContentGuard Pro - 内容安全检测系统

**ContentGuard Pro** 是一套基于 **Java Spring Boot** 构建的高性能、分层式内容安全检测服务。它创新性地结合了 **AC 自动机（Aho-Corasick）** 的毫秒级初筛能力与 LLM 的深度语义理解能力，旨在为社区、社交、评论等场景提供低成本、高精度的内容风控解决方案。

## 🌟 核心特性 (Key Features)

* **⚡ 分层防御架构**
  * **L0 防注入**: 基于特征匹配拦截 Prompt Injection（提示词注入）攻击。
  * **L1 极速初筛**: 集成 AC 自动机，支持百万级敏感词库的毫秒级匹配。
  * **L2 深度研判**: 对歧义内容及高风险用户触发 LLM 深度检测。
* **🤖 多模型支持**
  * 原生支持 **DeepSeek-V3** 和 **通义千问 (Qwen)** ，通过配置即可无缝切换。
  * 兼容所有 OpenAI 格式的 API 接口。
* **🎯 差异化检测策略**
  * **优质用户**: 执行长文本抽样检测，降低误伤率与成本。
  * **高风险用户**: 执行全量/分片检测，严防死守。
* **🚀 高并发与高可用**
  * **全链路异步**: 基于 `CompletableFuture` 编排业务流程，CPU/IO 线程池隔离。
  * **分布式限流**: 基于 Redis Lua 脚本实现精准的 API 限流。
  * **自动降级**: LLM 服务异常时自动回退至兜底策略，保障业务不中断。
* **🔄 动态热更新**
  * 支持敏感词库的定时热加载（默认 5 分钟），无需重启服务即可生效。

## 🛠️ 技术栈 (Tech Stack)

* **核心框架**: Spring Boot 3.2.0
* **持久层**: Spring Data JPA + MySQL 8.0
* **缓存与限流**: Redis
* **算法**: Aho-Corasick
* **网络客户端**: OkHttp 4
* **大模型接口**: DeepSeek API / Aliyun DashScope (Qwen)

## 🚀 快速开始 (Getting Started)

### 1. 环境准备

* JDK 17+
* Maven 3.6+
* MySQL 5.7/8.0
* Redis 6.0+

### 2. 数据库初始化

请在 MySQL 中创建数据库 `content_safety` 并执行以下 SQL 初始化表结构：

```
CREATE DATABASE `content_safety` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `content_safety`;

CREATE TABLE `sensitive_words` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `word` varchar(255) NOT NULL COMMENT '敏感词内容',
  `type` varchar(50) NOT NULL COMMENT '类型: HIGH_RISK(高风险), AMBIGUOUS(歧义/低风险)',
  `status` int(11) DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库';

-- 插入示例数据
INSERT INTO `sensitive_words` (`word`, `type`, `status`) VALUES 
('赌博网站', 'HIGH_RISK', 1),
('兼职刷单', 'AMBIGUOUS', 1);
```

### 3. 配置应用

修改 `src/main/resources/application.yml`：

1. **数据库连接**: 修改 `spring.datasource` 下的 username 和 password。
2. **Redis 连接**: 修改 `spring.data.redis` 配置。
3. **LLM 配置**: 在 `content-guard.llm` 下填入你的 API Key。

```
content-guard:
  llm:
    # 默认使用 DeepSeek，如需使用 Qwen 请切换注释
    api-url: "[https://api.deepseek.com/chat/completions](https://api.deepseek.com/chat/completions)"
    model: "deepseek-chat"
    api-keys:
      - "sk-your-real-api-key-here"
```

### 4. 启动服务

```
mvn clean package
java -jar target/content-guard-pro-1.0.0-RELEASE.jar
```

启动成功后，服务默认监听 `8080` 端口。

## 📖 API 文档 (API Reference)

### 核心检测接口

* **URL**: `/api/v1/content/check`
* **Method**: `POST`
* **Content-Type**: `application/json`

#### 请求示例

```
{
  "userId": "user_12345",
  "riskLevel": "LOW",
  "title": "测试文章标题",
  "fullContent": "这里是待检测的正文内容..."
}
```


| 字段        | 类型   | 必填 | 说明                               |
| ----------- | ------ | ---- | ---------------------------------- |
| userId      | String | 是   | 用户唯一标识                       |
| riskLevel   | Enum   | 是   | 用户风险等级:`HIGH`,`MEDIUM`,`LOW` |
| title       | String | 否   | 标题                               |
| fullContent | String | 是   | 正文全量内容                       |

#### 响应示例 (安全)

```
{
  "isSafe": true,
  "userId": "user_12345",
  "riskReason": null,
  "detectStrategy": "Quick-Pass",
  "detectTime": 15
}
```

#### 响应示例 (违规)

```
{
  "isSafe": false,
  "userId": "user_12345",
  "riskReason": [
    {
      "sensitiveType": "LLM_DETECTED_POLITICAL",
      "sensitiveFragment": "敏感语句片段"
    }
  ],
  "detectStrategy": "Ambiguous-Check",
  "detectTime": 850
}
```

## ⚙️ 高级配置指南

所有业务参数均可在 `application.yml` 中调整：


| 配置项                                            | 默认值 | 说明                                   |
| ------------------------------------------------- | ------ | -------------------------------------- |
| `content-guard.text.premium-threshold`            | 500    | 优质用户全文检测的长度阈值，超过则抽样 |
| `content-guard.security.ac-refresh-rate-ms`       | 300000 | 敏感词库自动刷新间隔 (毫秒)            |
| `content-guard.llm.rate-limit.permits-per-second` | 20     | LLM 接口的 QPS 限制                    |
| `content-guard.async.io.max-pool-size`            | 100    | LLM 并发调用的最大线程数               |

## 🧪 测试建议

本项目已针对 **Apifox** 进行了测试用例设计。建议覆盖以下场景：

1. **Prompt 注入测试**: 输入 "忽略之前的指令" 验证是否被拦截。
2. **高风险词阻断**: 输入数据库中定义的 `HIGH_RISK` 词汇。
3. **歧义词透传**: 输入 `AMBIGUOUS` 词汇，验证是否触发 LLM 调用。
