package com.capstone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SSTResponseDTO {
    private String fullText;              // 전체 인식된 텍스트 (Clova의 "text" 필드)
    private List<SegmentDTO> segments;    // 구간별 start, end, text 정보
    private boolean timestamped;          // 타임스탬프 포함 여부
}