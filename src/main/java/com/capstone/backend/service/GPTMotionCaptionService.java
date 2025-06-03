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
            너는 교육 전문가로서 강의자의 비언어적 표현에 따라 강의를 평가하는 역할을 맡고 있다.
            
            아래는 강의자의 강의 중 비언어적 표현이다. 형식은 다음과 같이 제공된다
            ※ 모션 데이터 형식: [start_mm:start_ss : end_mm:end_ss] : label
            ※ label_map = {
                0: "서있음",
                1: "손 머리에 대는 중",
                2: "뒤 돌고 있음",
                3: "팔짱끼는 중",
                4: "고개를 숙이고 있음"
              }
              
            강의자의 비언어적 표현 데이터 : {motion}
              
            제공된 데이터를 바탕으로 가장 지양해야 할 비언어적 표현이 담긴 구간 두 개를 다음 형식에 맞추어 제공하라.
            
            ※ 결과물 형식
            ***** [동작 시작 분:동작 시작 초], [동작 종료 분:동작 종료 초] : label
            @@@@@ 이유 : 한 문장 이상의 선정 이유
            
            ***** [start_mm:start_ss], [end_mm:end_ss] : label
            @@@@@ 이유 : 한 문장 이상의 선정 이유
            
            ※ 결과물 예시
            ***** [00:59], [01:04] : 고개를 숙이고 있음
            @@@@@ 이유 : 강의 중 고개를 숙이고 있는 것은 청중과의 시선 교환을 방해하여 청중의 집중력을 떨어뜨릴 수 있습니다
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
        body.put("temperature", 0.1);

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
