package com.capstone.backend.controller;

import com.capstone.backend.dto.LectureUploadAudioRespondDTO;
import com.capstone.backend.dto.LectureConfigRequestDTO;
import com.capstone.backend.dto.LectureUploadConfigRespondDTO;
import com.capstone.backend.service.ClovaSpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class LectureUploadController {

    private final ClovaSpeechService clovaSpeechService;

    @PostMapping("/audio")
    public ResponseEntity<LectureUploadAudioRespondDTO> uploadLectureAudio(@RequestParam MultipartFile file) {
        try {
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path uploadDir = projectRoot.resolve("uploads");
            Files.createDirectories(uploadDir);

            // 파일 확장자 유지
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String timestamp = String.valueOf(System.currentTimeMillis());
            Path savedPath = uploadDir.resolve("lecture_" + timestamp + extension);

            file.transferTo(savedPath.toFile());

            // Clova 처리 (메서드명이 sendAudioToClova인지 확인)
            String transcript = clovaSpeechService.sendAudioToClova(savedPath.toFile());

            Files.deleteIfExists(savedPath);

            LectureUploadAudioRespondDTO responseDto = new LectureUploadAudioRespondDTO("success", transcript);
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            e.printStackTrace();
            LectureUploadAudioRespondDTO errorDto =
                    new LectureUploadAudioRespondDTO("fail", "오류 발생: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorDto);
        }
    }
    @PostMapping("/config") // POST 요청 처리
    public ResponseEntity<?> uploadLectureConfig(@RequestBody LectureConfigRequestDTO request) {
        // 👉 여기에 실제 저장/처리 로직을 넣으면 됨

        // 예시 로그 출력
        System.out.println("📥 수신된 설정: " + request);

        // 응답 반환
        return ResponseEntity.ok(new LectureUploadConfigRespondDTO("success"));
    }
}