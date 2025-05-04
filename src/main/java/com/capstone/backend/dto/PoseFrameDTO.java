package com.capstone.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 하나의 타임스탬프에 대한 랜드마크 묶음
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PoseFrameDTO {
    private long timestamp;
    private List<PoseLandmarkDTO> results;
}
