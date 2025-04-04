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
            // 파일 확장자 검사
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
                return ResponseEntity.badRequest()
                        .body(new LectureUploadAudioRespondDTO("fail", "MP3 파일만 업로드 가능합니다."));
            }

            // Clova에 InputStream 직접 전달
            String transcript = clovaSpeechService.sendAudioToClova(file.getInputStream());

            // 응답 생성
            LectureUploadAudioRespondDTO responseDto = new LectureUploadAudioRespondDTO("success", transcript);
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new LectureUploadAudioRespondDTO("fail", "오류 발생: " + e.getMessage()));
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