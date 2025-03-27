package com.capstone.backend.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;

@Service
public class MediaConvertService {

    private final String ffmpegPath = "C:/Users/g2hyeong/Downloads/ffmpeg-N-118896-g9f0970ee35-win64-gpl-shared/ffmpeg-N-118896-g9f0970ee35-win64-gpl-shared/bin/ffmpeg.exe";

    public File convertToMp3(File mp4File) {
        try {
            Path mp4Path = mp4File.toPath();
            Path parentDir = mp4Path.getParent();
            String baseName = mp4Path.getFileName().toString().replace(".mp4", "");
            Path mp3Path = parentDir.resolve(baseName + ".mp3");

            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-i", mp4Path.toString(),
                    "-vn",
                    "-acodec", "libmp3lame",
                    mp3Path.toString()
            );
            pb.inheritIO().start().waitFor();

            return mp3Path.toFile();
        } catch (Exception e) {
            throw new RuntimeException("ffmpeg 변환 실패: " + e.getMessage(), e);
        }
    }
}
