package com.safety.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectRequest {
    private String userId;
    private RiskLevel riskLevel;
    private String title;
    private String fullContent;
}
