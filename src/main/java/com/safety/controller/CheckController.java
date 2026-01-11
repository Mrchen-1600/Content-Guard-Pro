package com.safety.controller;

import com.safety.model.DetectRequest;
import com.safety.model.DetectResponse;
import com.safety.service.SecurityOrchestrator;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/content")
@AllArgsConstructor
public class CheckController {

    private final SecurityOrchestrator orchestrator;

    /**
     * 内容安全检测接口
     */
    @PostMapping("/check")
    public CompletableFuture<DetectResponse> check(@RequestBody DetectRequest request) {
        return orchestrator.checkContent(request);
    }
}