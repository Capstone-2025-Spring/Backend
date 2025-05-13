package com.capstone.backend.controller;

import com.capstone.backend.dto.MotionRangeSplitDTO;
import com.capstone.backend.service.MotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/motion")
public class MotionController {

    private final MotionService motionService;
    @PostMapping("/split")
    public ResponseEntity<MotionRangeSplitDTO> getMotionCaptionsSplit(@RequestParam("file") MultipartFile file) {
        try {
            byte[] json = file.getBytes();
            String jsonResponse = motionService.getCaptionResult(json);
            MotionRangeSplitDTO result = motionService.splitMotionCaptionByRange(jsonResponse, 120, 150);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/split/range")
    public ResponseEntity<String> getMotionCaptionsSplitRange(@RequestParam("file") MultipartFile file) {
        try {
            byte[] json = file.getBytes();
            String jsonResponse = motionService.getCaptionResult(json);
            MotionRangeSplitDTO result = motionService.splitMotionCaptionByRange(jsonResponse, 120, 150);
            String rangeMotionCaption = motionService.formatRangeCaptionsCompressed(result.getRangeCaptions());
            return ResponseEntity.ok(rangeMotionCaption);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
