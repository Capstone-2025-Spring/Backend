package com.capstone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MotionCaptionDTO {
    private String from;
    private String to;
    private int startSecond;
    private int endSecond;
    private String label;
}
