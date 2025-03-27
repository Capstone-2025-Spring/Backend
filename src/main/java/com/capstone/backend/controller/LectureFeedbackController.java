package com.capstone.backend.controller;

import com.capstone.backend.service.LectureFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lecture")
public class LectureFeedbackController {

    private final LectureFeedbackService lectureFeedbackService;

    @PostMapping("/feedback")
    public ResponseEntity<String> uploadLectureAndGetFeedback(@RequestParam("file") MultipartFile file) {
        try {
            // 1. MP4 파일 임시 저장
            Path tempFile = Files.createTempFile("lecture-", ".mp4");
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            File mp4File = tempFile.toFile();

            // 2. 전체 흐름 실행 (mp4 → mp3 → Clova → GPT)
            String feedback = lectureFeedbackService.generateFeedbackFromLecture(mp4File);

            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }
}
