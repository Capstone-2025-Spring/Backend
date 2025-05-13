package com.capstone.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class EvaluationResultDTO {
    private double overallScore;
    private String overallReason;
    private List<EvaluationItemDTO> criteriaScores;
}
