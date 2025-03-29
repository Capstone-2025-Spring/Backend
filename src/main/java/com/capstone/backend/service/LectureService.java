package com.capstone.backend.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Service
public class LectureService {

    private final ClovaSpeechService clovaSpeechService;
    private final PythonClient pythonClient;

    public LectureService(ClovaSpeechService clovaSpeechService, PythonClient pythonClient) {
        this.clovaSpeechService = clovaSpeechService;
        this.pythonClient = pythonClient;
    }

    public void analyzeLecture(File wavFile) {
        // 1. Clova로 텍스트 변환
        String text = clovaSpeechService.recognizeSpeech(wavFile);

        // 2. Python에 텍스트 전달
        Map<String, Object> result = pythonClient.sendTextToPython(text);

        // 3. 결과 출력 or 저장
        System.out.println("Known words: " + result.get("known_words"));
        System.out.println("Unknown words: " + result.get("unknown_words"));
    }
}
