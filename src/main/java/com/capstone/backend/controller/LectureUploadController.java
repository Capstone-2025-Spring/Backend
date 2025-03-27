package com.capstone.backend.controller;

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

    @PostMapping("/upload")
    public ResponseEntity<String> uploadLecture(@RequestParam MultipartFile file) {
        try {
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path uploadDir = projectRoot.resolve("uploads");
            Files.createDirectories(uploadDir);

            String timestamp = String.valueOf(System.currentTimeMillis());
            Path mp4Path = uploadDir.resolve("lecture_" + timestamp + ".mp4");
            Path mp3Path = uploadDir.resolve("lecture_" + timestamp + ".mp3");

            file.transferTo(mp4Path.toFile());

            String ffmpegPath = "C:/Users/g2hyeong/Downloads/ffmpeg-N-118896-g9f0970ee35-win64-gpl-shared/ffmpeg-N-118896-g9f0970ee35-win64-gpl-shared/bin/ffmpeg.exe";

            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-i", mp4Path.toString(),
                    "-vn",
                    "-acodec", "libmp3lame",
                    mp3Path.toString()
            );
            pb.inheritIO().start().waitFor();

            String transcript = clovaSpeechService.sendAudioToClova(mp3Path.toFile());

            Files.deleteIfExists(mp4Path);
            Files.deleteIfExists(mp3Path);

            return ResponseEntity.ok(transcript);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("오류 발생: " + e.getMessage());
        }
    }
}
