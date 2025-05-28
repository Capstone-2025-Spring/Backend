package com.capstone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotionEvaluationSplitDTO {
    private String startMin;
    private String startSec;
    private String endMin;
    private String endSec;
    private String label;
    private String reason;
}
