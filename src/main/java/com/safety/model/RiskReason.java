package com.safety.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskReason {
    /**
     * 违规类型：如 HIGH_CONFIDENCE, AMBIGUOUS, INJECTION 等
     */
    private String sensitiveType;
    /**
     * 违规片段
     */
    private String sensitiveFragment;
}