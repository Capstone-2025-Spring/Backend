package com.capstone.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class LectureFeedbackService {

    private final ClovaSpeechService clovaSpeechService;
    private final GptService gptService;
    private final MediaConvertService mediaConvertService;

    public String generateFeedbackFromLecture(File mp4File) {
        // 1. MP4 → MP3 변환
        File mp3File = mediaConvertService.convertToMp3(mp4File);

        // 2. Clova로 텍스트 추출 (result.text)
        String transcript = clovaSpeechService.recognizeSpeech(mp3File);

        // 3. GPT 피드백 생성
        return gptService.getFeedbackFromGpt(transcript);
    }
}
