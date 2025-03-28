package com.capstone.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class LectureFeedbackService {

    private final ClovaSpeechService clovaSpeechService;
    private final GptService gptService;

    public String generateFeedbackFromLecture(File wavFile) {
        // 1. Clova로 텍스트 추출
        String transcript = clovaSpeechService.recognizeSpeech(wavFile);

        // 2. GPT 피드백 생성
        return gptService.getFeedbackFromGpt(transcript);
    }
}
