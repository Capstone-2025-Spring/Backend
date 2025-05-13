package com.capstone.backend.controller;

import com.capstone.backend.dto.SSTRangeSplitDTO;
import com.capstone.backend.dto.SSTResponseDTO;
import com.capstone.backend.dto.SegmentDTO;
import com.capstone.backend.service.ClovaSpeechService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sst")
public class SSTController {

    private final ClovaSpeechService clovaSpeechService;
    @PostMapping("/timestamp")
    public ResponseEntity<SSTResponseDTO> getStructuredSSTWithTimestamps(@RequestParam("file") MultipartFile file) {
        try {
            // MultipartFile → File 변환
            File tempFile = File.createTempFile("upload_", ".mp3");
            file.transferTo(tempFile);

            // 파싱까지 포함된 결과 반환
            SSTResponseDTO response = clovaSpeechService.sendAudioToClovaWithTimestamps(tempFile);

            tempFile.delete(); // 임시 파일 삭제
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/timestamp/split")
    public ResponseEntity<SSTRangeSplitDTO> getSSTSplitByRange(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = File.createTempFile("upload_", ".mp3");
            file.transferTo(tempFile);

            SSTResponseDTO response = clovaSpeechService.sendAudioToClovaWithTimestamps(tempFile);
            tempFile.delete();

            SSTRangeSplitDTO splitDto = clovaSpeechService.splitByTimeRange(response, 120_000, 150_000);
            return ResponseEntity.ok(splitDto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

}
