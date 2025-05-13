package com.capstone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SSTRangeSplitDTO {
    private List<SegmentDTO> fullSegments;     // 전체
    private List<SegmentDTO> rangeSegments;    // 범위 내 (120000~150000)
    private List<SegmentDTO> otherSegments;    // 범위 외
}
