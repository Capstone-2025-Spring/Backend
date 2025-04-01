package com.capstone.backend.controller;

import com.capstone.backend.dto.LectureUploadAudioRespondDTO;
import com.capstone.backend.dto.LectureConfigRequestDTO;
import com.capstone.backend.service.ClovaSpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


// 전체 삭제 예정
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class temp {

    private final ClovaSpeechService clovaSpeechService;

    @PostMapping("/upload-audio")
    public ResponseEntity<LectureUploadAudioRespondDTO> uploadLectureAudio(@RequestParam MultipartFile file) {
        try {
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path uploadDir = projectRoot.resolve("uploads");
            Files.createDirectories(uploadDir);

            String timestamp = String.valueOf(System.currentTimeMillis());
            Path wavPath = uploadDir.resolve("lecture_" + timestamp + ".wav");
            file.transferTo(wavPath.toFile());

            String transcript = clovaSpeechService.sendAudioToClova2(wavPath.toFile());

            Files.deleteIfExists(wavPath);

            LectureUploadAudioRespondDTO responseDto = new LectureUploadAudioRespondDTO("success", transcript);
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            e.printStackTrace();
            LectureUploadAudioRespondDTO errorDto =
                    new LectureUploadAudioRespondDTO("fail", "오류 발생: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorDto);
        }
    }

    @PostMapping("/upload-config") // POST 요청 처리
    public ResponseEntity<?> uploadUserOptions(@RequestBody LectureConfigRequestDTO request) {
        // 👉 여기에 실제 저장/처리 로직을 넣으면 됨

        // 예시 로그 출력
        System.out.println("📥 수신된 설정: " + request);

        // 응답 반환
        return ResponseEntity.ok(new LectureUploadAudioRespondDTO("설정 업로드 완료", "success"));
    }
}