package com.capstone.backend.service;

import com.capstone.backend.config.OpenAiProperties;
import com.capstone.backend.dto.MotionEvaluationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GPTMotionCaptionService {
    private final OpenAiProperties openAiProperties;
    private final EvaluationParserService evaluationParserService;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String promptTemplate = """
            너는 교육 전문가로서 강의자의 비언어적 표현에 따라 강의를 엄격하게 평가해야한다.
             
             아래는 강의자의 강의 중 비언어적 표현 데이터이다. 형식은 다음과 같다:
             ※ 모션 데이터 형식: [start_mm:start_ss : end_mm:end_ss] : label
             ※ label_map = {
                 0: "서있음",
                 1: "손 머리에 대는 중",
                 2: "뒤 돌고 있음",
                 3: "팔짱끼는 중",
                 4: "고개를 숙이고 있음"
               }
             서있음을 제외한 동작들은, 강의에 부정적인 영향을 주는 요소이므로 평가에 반영해야 한다.
             강의자의 비언어적 표현 데이터: {motion}
             
             다음 조건에 따라 **"서있음"을 제외하고 가장 오랜 기간 지속되는 서로 다른 종류의 비언어적 표현 구간 2개**를 골라 와서 평가하고 아래와 같이 **구조화된 JSON 형식**으로만 출력하라:
             
              출력 JSON은 다음과 같은 구조를 반드시 따른다:
             - startMin: 문자열, 시작 분 (예: "00")
             - startSec: 문자열, 시작 초 (예: "59")
             - endMin: 문자열, 종료 분 (예: "01")
             - endSec: 문자열, 종료 초 (예: "04")
             - label: 문자열, 비언어적 표현
             - reason: 문자열, 한 문장 이상의 설명
             
              출력 예시:
             [
               {
                 "startMin": "00",
                 "startSec": "59",
                 "endMin": "01",
                 "endSec": "04",
                 "label": "고개를 숙이고 있음",
                 "reason": "강의 중 고개를 숙이는 자세는 자신감 부족으로 인식될 수 있다."
               },
               {
                 "startMin": "01",
                 "startSec": "33",
                 "endMin": "01",
                 "endSec": "39",
                 "label": "뒤 돌고 있음",
                 "reason": "청중에게 등을 보이는 행동은 소통을 방해한다."
               }
             ]
            """;
    public String fillMotionPrompt(String motionInfo) {
        return promptTemplate.replace("{motion}", motionInfo);
    }

    public MotionEvaluationDTO getMotionCaptions(String motionInfo) {
        String prompt = fillMotionPrompt(motionInfo);

        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", prompt
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiProperties.getModel());
        body.put("messages", List.of(systemMessage));
        body.put("temperature", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> messageResp = (Map<String, Object>) choices.get(0).get("message");

        String gptResponse = messageResp.get("content").toString().trim();

        System.out.println("모션 결과 분석\n");
        System.out.println(gptResponse);

        // 🔽 비언어적 표현 구간 분석 파서 호출
        return evaluationParserService.parseMotionCaptions(gptResponse);
    }

}
