package com.safety.service;

import com.safety.model.DetectRequest;
import com.safety.model.DetectResponse;
import com.safety.model.RiskLevel;
import com.safety.model.RiskReason;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Emit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class SecurityOrchestrator {
    private final AcAutomatonService acService;
    private final LLMInfrastructure llmInfrastructure;
    private final TextProcessingService textProcessingService;
    private final Executor ioExecutor;
    private final Executor cpuExecutor;

    public SecurityOrchestrator(AcAutomatonService acService,
                                LLMInfrastructure llmInfrastructure,
                                TextProcessingService textProcessingService,
                                @Qualifier("ioExecutor") Executor ioExecutor,
                                @Qualifier("cpuExecutor") Executor cpuExecutor) {
        this.acService = acService;
        this.llmInfrastructure = llmInfrastructure;
        this.textProcessingService = textProcessingService;
        this.ioExecutor = ioExecutor;
        this.cpuExecutor = cpuExecutor;
    }

    public CompletableFuture<DetectResponse> checkContent(DetectRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 注入检测
        if (llmInfrastructure.containsInjection(request.getFullContent()) ||
                llmInfrastructure.containsInjection(request.getTitle())) {
            log.info("用户 {} 触发注入防御", request.getUserId());
            return CompletableFuture.completedFuture(
                    buildResponse(request.getUserId(), false, "INJECTION_ATTACK", "检测到注入指令", "Pre-Check", startTime)
            );
        }

        // 2. AC 初筛
        return CompletableFuture.supplyAsync(() -> {
            Collection<Emit> highRiskEmits = acService.checkHighRisk(request.getFullContent());
            if (!highRiskEmits.isEmpty()) {
                Emit first = highRiskEmits.iterator().next();
                // 抛出异常由 exceptionally 捕获
                throw new SecurityException("HIGH_RISK_KEYWORD:" + first.getKeyword());
            }

            Collection<Emit> ambiguousEmits = acService.checkAmbiguous(request.getFullContent());
            return !ambiguousEmits.isEmpty();
        }, cpuExecutor).thenCompose(hitAmbiguous -> {

            // 3. 决策
            if (hitAmbiguous) {
                log.info("用户 {} 命中歧义词，透传 LLM", request.getUserId());
                // 使用注入的 textProcessingService
                String contextText = textProcessingService.extractSample(request.getTitle(), request.getFullContent());
                return callLlmWithFallback(contextText, request, "Ambiguous-Check", startTime, true);
            } else {
                if (request.getRiskLevel() == RiskLevel.LOW) {
                    return CompletableFuture.completedFuture(
                            buildResponse(request.getUserId(), true, null, null, "Quick-Pass", startTime)
                    );
                } else {
                    return dispatchDeepScan(request, startTime);
                }
            }

        }).exceptionally(ex -> {
            if (ex.getCause() instanceof SecurityException) {
                String msg = ex.getCause().getMessage();
                log.info("用户 {} 命中高风险词库: {}", request.getUserId(), msg);
                String[] parts = msg.split(":");
                return buildResponse(request.getUserId(), false, "HIGH_CONFIDENCE_BLOCK", parts.length > 1 ? parts[1] : "Keyword", "AC-Block", startTime);
            }
            log.error("用户 {} 检测过程发生系统异常", request.getUserId(), ex);
            return buildResponse(request.getUserId(), false, "SYSTEM_ERROR", "Internal Error", "Error", startTime);
        });
    }

    private CompletableFuture<DetectResponse> dispatchDeepScan(DetectRequest request, long startTime) {
        String strategy = "Sampling-Scan";
        // 使用注入的 Service 处理文本
        String textToSend = textProcessingService.extractSample(request.getTitle(), request.getFullContent());
        return callLlmWithFallback(textToSend, request, strategy, startTime, false);
    }

    private CompletableFuture<DetectResponse> callLlmWithFallback(String text, DetectRequest req, String strategy, long startTime, boolean wasAmbiguous) {
        return llmInfrastructure.analyzeAsync(text, req.getTitle())
                .handleAsync((result, ex) -> {
                    if (ex == null && result != null) {
                        return buildResponse(req.getUserId(), result.isSafe(),
                                result.isSafe() ? null : "LLM_DETECTED_" + result.getType(),
                                result.getSnippet(), strategy, startTime);
                    }

                    log.error("用户 {} LLM 调用失败，触发兜底。Cause: {}", req.getUserId(), ex != null ? ex.getMessage() : "Unknown");

                    if (wasAmbiguous) {
                        return buildResponse(req.getUserId(), false, "FALLBACK_BLOCK", "Ambiguous hit & LLM failed", "Fallback-Strict", startTime);
                    } else {
                        return buildResponse(req.getUserId(), true, null, null, "Fallback-Pass", startTime);
                    }
                }, ioExecutor);
    }

    private DetectResponse buildResponse(String uid, boolean safe, String type, String snippet, String strategy, long start) {
        DetectResponse.DetectResponseBuilder builder = DetectResponse.builder()
                .userId(uid)
                .isSafe(safe)
                .detectStrategy(strategy)
                .detectTime(System.currentTimeMillis() - start);

        if (!safe) {
            builder.riskReason(Collections.singletonList(new RiskReason(type, snippet)));
        }
        return builder.build();
    }
}