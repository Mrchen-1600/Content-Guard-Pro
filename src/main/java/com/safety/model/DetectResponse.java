package com.safety.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectResponse {
    private boolean isSafe;
    private String userId;
    private List<RiskReason> riskReason;
    /**
     * 本次检测策略：抽样检测/全量分片检测/快速通过 等
     */
    private String detectStrategy;
    private Long detectTime;
}