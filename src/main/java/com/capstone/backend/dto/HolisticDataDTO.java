package com.capstone.backend.dto;

import lombok.Data;
import java.util.List;

// 1. 각 Landmark Point 정의
@Data
public class PoseLandmark {
    private double x;
    private double y;
    private double z;
    private double visibility;
}

// 2. 하나의 타임스탬프에 해당하는 Landmark 묶음
@Data
public class PoseFrame {
    private long timestamp;
    private List<PoseLandmark> results;
}

// 3. 전체 데이터 전송 구조
@Data
public class HolisticDataDTO {
    private String videoId;
    private List<PoseFrame> holisticData;
}
