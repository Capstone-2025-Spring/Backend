package com.capstone.backend.controller;

import com.capstone.backend.dto.*;
import com.capstone.backend.entity.Config;
import com.capstone.backend.entity.Holistic;
import com.capstone.backend.entity.MotionCaption;
import com.capstone.backend.entity.SST;
import com.capstone.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class LectureUploadController {

    private final ClovaSpeechService clovaSpeechService;
    private final ConfigService configService;
    private final MotionCaptionService motionCaptionService;
    private final SSTService sstService;

    // 🎧 오디오 업로드 및 STT 처리
    @PostMapping("/audio-clova")
    public ResponseEntity<LectureUploadAudioRespondDTO> uploadLectureAudioToClova(@RequestParam MultipartFile file) {
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

    // ⚙️ 설정 조회 (최신)
    @GetMapping("/config")
    public ResponseEntity<ConfigRequestDTO> getLatestConfig() {
        return configService.findLatestAsDTO()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ⚙️ 설정 전체 리스트 조회
    @GetMapping("/config/all")
    public ResponseEntity<List<ConfigRequestDTO>> getAllConfigs() {
        return ResponseEntity.ok(configService.findAllAsDTO());
    }


    // 🧠 SST 저장
    @PostMapping("/sst")
    public ResponseEntity<String> uploadSST(@RequestBody String content) {
        sstService.save(content);
        return ResponseEntity.ok("✅ SST 저장 완료");
    }

    // 🧠 SST 조회
    @GetMapping("/sst")
    public ResponseEntity<SST> getLatestSST() {
        return sstService.getLatestSST()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🧠 SST 전체 리스트 조회
    @GetMapping("/sst/all")
    public ResponseEntity<List<SST>> getAllSST() {
        return ResponseEntity.ok(sstService.findAll());
    }

    // 🎬 MotionCaption 저장
    @PostMapping("/motion")
    public ResponseEntity<String> uploadMotionCaption(@RequestBody String content) {
        motionCaptionService.save(content);
        return ResponseEntity.ok("✅ MotionCaption 저장 완료");
    }

    // 🎬 MotionCaption 조회
    @GetMapping("/motion")
    public ResponseEntity<MotionCaption> getLatestMotionCaption() {
        return motionCaptionService.findLatest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🎬 MotionCaption 전체 리스트 조회
    @GetMapping("/motion/all")
    public ResponseEntity<List<MotionCaption>> getAllMotionCaptions() {
        return ResponseEntity.ok(motionCaptionService.findAll());
    }
}
