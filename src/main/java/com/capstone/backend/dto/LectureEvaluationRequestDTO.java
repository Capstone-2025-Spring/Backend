package com.capstone.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LectureEvaluationRequestDTO {
    private String lectureText;
    private String audioInfo;
    private String motionInfo;
}
