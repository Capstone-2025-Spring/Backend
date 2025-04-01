package com.capstone.backend.controller;

import com.capstone.backend.dto.LectureUploadAudioRespondDTO;
import com.capstone.backend.service.ClovaSpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LectureUploadController {

    private final ClovaSpeechService clovaSpeechService;

    @PostMapping("/upload/audio")
    public ResponseEntity<LectureUploadAudioRespondDTO> uploadLectureAudio(@RequestParam MultipartFile file) {
        try {
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path uploadDir = projectRoot.resolve("uploads");
            Files.createDirectories(uploadDir);

            String timestamp = String.valueOf(System.currentTimeMillis());
            Path wavPath = uploadDir.resolve("lecture_" + timestamp + ".wav");
            file.transferTo(wavPath.toFile());

            String transcript = clovaSpeechService.recognizeSpeech(wavPath.toFile());

            Files.deleteIfExists(wavPath);

            LectureUploadAudioRespondDTO responseDto = new LectureUploadAudioRespondDTO(true, transcript);
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            e.printStackTrace();
            LectureUploadAudioRespondDTO errorDto =
                    new LectureUploadAudioRespondDTO(false, "오류 발생: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorDto);
        }
    }

}
