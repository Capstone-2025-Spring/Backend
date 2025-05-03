package com.capstone.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class HolisticDataDTO {
    private String videoId;
    private List<Object> poseLandmarks; // 자세한 타입 필요 시 Object 대신 커스텀 클래스
}