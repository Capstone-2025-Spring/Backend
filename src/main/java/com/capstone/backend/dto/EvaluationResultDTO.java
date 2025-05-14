package com.capstone.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationResultDTO {
    private double overallScore;
    private String overallReason;
    private List<EvaluationItemDTO> criteriaScores;

    // 🔽 추가된 필드들
    private String vocabDifficulty;         // 예: "보통", "어려움"
    private List<String> blockedWords;      // 금지된 어휘 목록

    //event 관련 응답
    private String eventScore;
    private String eventReason;
}
