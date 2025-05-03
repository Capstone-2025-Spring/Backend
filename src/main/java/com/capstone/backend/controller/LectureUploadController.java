package com.capstone.backend.controller;

import com.capstone.backend.dto.*;
import com.capstone.backend.entity.Config;
import com.capstone.backend.service.ClovaSpeechService;
import com.capstone.backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class LectureUploadController {

    private final ClovaSpeechService clovaSpeechService;
    private final ConfigService configService;

    // 🎧 오디오 업로드 및 STT 처리
    @PostMapping("/audio")
    public ResponseEntity<LectureUploadAudioRespondDTO> uploadLectureAudio(@RequestParam MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
                return ResponseEntity.badRequest()
                        .body(new LectureUploadAudioRespondDTO("fail", "MP3 파일만 업로드 가능합니다."));
            }

            // Clova API로 STT 수행
            String transcript = clovaSpeechService.sendAudioToClova(file.getInputStream());

            return ResponseEntity.ok(new LectureUploadAudioRespondDTO("success", transcript));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new LectureUploadAudioRespondDTO("fail", "오류 발생: " + e.getMessage()));
        }
    }

    // ⚙️ 설정 저장
    @PostMapping("/config")
    public ResponseEntity<LectureUploadConfigRespondDTO> uploadLectureConfig(@RequestBody ConfigRequestDTO request) {
        Config saved = configService.save(request);
        System.out.println("📥 수신된 설정: " + request);
        return ResponseEntity.ok(new LectureUploadConfigRespondDTO("success"));
    }

    // 🧍‍♂️ 포즈 랜드마크 업로드
    @PostMapping("/holistic")
    public ResponseEntity<String> uploadHolisticData(@RequestBody HolisticDataDTO request) {
        System.out.println("📥 Holistic Data Received:");
        System.out.println("Video ID: " + request.getVideoId());
        System.out.println("Pose Count: " +
                (request.getHolisticData() != null ? request.getHolisticData().size() : 0));

        // TODO: holisticDataService.save(request);

        return ResponseEntity.ok("Holistic data received successfully");
    }
}
