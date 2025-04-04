package com.capstone.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class LectureFeedbackService {

    private final ClovaSpeechService clovaSpeechService;
    private final GptService gptService;

    public String generateFeedbackFromLecture(InputStream mp3Stream) {
        // 1. Clova로 텍스트 추출 (InputStream 기반으로 변경)
        String transcript = clovaSpeechService.sendAudioToClova(mp3Stream);

        // 2. GPT 피드백 생성
        //return gptService.getFeedbackFromGpt(transcript);
        return "";
    }
}
