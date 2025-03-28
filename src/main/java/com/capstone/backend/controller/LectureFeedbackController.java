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
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lecture")
public class LectureFeedbackController {

    private final LectureFeedbackService lectureFeedbackService;

    @PostMapping("/feedback")
    public ResponseEntity<String> uploadLectureAndGetFeedback(@RequestParam("file") MultipartFile file) {
        try {
            // 1. WAV 파일 임시 저장
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
            Files.createDirectories(uploadDir);

            String filename = "lecture-" + System.currentTimeMillis() + ".wav";
            Path wavPath = uploadDir.resolve(filename);
            file.transferTo(wavPath.toFile());

            // 2. 전체 흐름 실행 (wav → Clova → GPT)
            String feedback = lectureFeedbackService.generateFeedbackFromLecture(wavPath.toFile());

            // 3. 임시 파일 삭제
            Files.deleteIfExists(wavPath);

            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }
}
