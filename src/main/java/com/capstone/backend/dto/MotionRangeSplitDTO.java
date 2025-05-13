package com.capstone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotionRangeSplitDTO {
    private List<MotionCaptionDTO> fullCaptions;
    private List<MotionCaptionDTO> rangeCaptions;
    private List<MotionCaptionDTO> otherCaptions;
}
