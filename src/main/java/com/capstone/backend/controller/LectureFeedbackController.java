package com.capstone.backend.controller;

import com.capstone.backend.dto.LectureEvaluationRequestDTO;
import com.capstone.backend.service.GptService;
import com.capstone.backend.service.LectureFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lecture")
public class LectureFeedbackController {

    private final LectureFeedbackService lectureFeedbackService;
    private final GptService gptService;

    @PostMapping("/feedback")
    public ResponseEntity<String> uploadLectureAndGetFeedback(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 파일 타입 확인 (확장자, Content-Type)
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
                return ResponseEntity.badRequest().body("MP3 파일만 업로드 가능합니다.");
            }

            if (!file.getContentType().equalsIgnoreCase("audio/mpeg")) {
                return ResponseEntity.badRequest().body("Content-Type은 audio/mpeg(MP3) 형식이어야 합니다.");
            }

            // 2. 서비스에 InputStream으로 바로 넘김
            String feedback = lectureFeedbackService.generateFeedbackFromLecture(file.getInputStream());

            return ResponseEntity.ok(feedback);

        } catch (Exception e) {
            e.printStackTrace(); // 디버깅용 로그
            return ResponseEntity.internalServerError().body("서버 처리 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/prompt/test")
    public ResponseEntity<String> getFullEvaluationPipeline(@RequestBody LectureEvaluationRequestDTO requestDTO) {
        try {
            String result = gptService.runFullEvaluationPipeline(
                    requestDTO.getLectureText(),
                    requestDTO.getAudioInfo(),
                    requestDTO.getMotionInfo()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }
}
