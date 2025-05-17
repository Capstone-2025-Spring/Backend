package com.capstone.backend.service;

import com.capstone.backend.config.OpenAiProperties;
import com.capstone.backend.dto.GptRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class GptRecommendationService {

    private final OpenAiProperties openAiProperties;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public GptRecommendationResponse recommend(String userMessage) {
        // 1. 프롬프트 구성
        String fullPrompt = String.format("""
                너는 상담가야. 너는 초보 강사의 메세지를 받을껀데, 상담자가 본인의 성격이나 mbti같은 개인적인 성향을 설명해줄꺼야.
                너는 그 설명을 읽고 대답을하고 공감을 해주면서 강의 스타일을 추천해줘야해. 
                아래 사용자의 메시지를 읽고, 너가 추천해주는 강의 스타일을 **1~2개의 단어**로 추천해줘. 
                추천 단어는 파싱하기 좋게 `^^^^`를 단어 앞뒤에 붙여서 출력해야 해. 
                예: 아하~~ 너는 정말 사랑스러운 사람이구나 ! 내 생각에는 ^^^^친절함^^^^ 이 기준이 너에게 좋을것 같아~! 너가 원하는 다른 스타일이 있을까 ?, 이런 식으로 반드시 중요한 단어 앞뒤로 ^^^^가 붙어있어야 해.
                 꼭 친절하게 상담도 함께 해줘. 
                
                사용자의 메시지:
                "%s"
                """, userMessage);

        // 2. 메시지 구성
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", fullPrompt
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiProperties.getModel());
        body.put("messages", List.of(systemMessage));
        body.put("temperature", 0.7);

        // 3. 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 4. GPT 호출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        // 5. 응답 파싱
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> messageResp = (Map<String, Object>) choices.get(0).get("message");
            String reply = messageResp.get("content").toString().trim();

            return new GptRecommendationResponse(reply);
        } catch (Exception e) {
            return new GptRecommendationResponse("추천 중 오류가 발생했습니다. 다시 시도해 주세요.");
        }
    }
}
