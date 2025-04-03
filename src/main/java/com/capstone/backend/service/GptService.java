package com.capstone.backend.service;

import com.capstone.backend.config.OpenAiProperties;
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
public class GptService {

    private final OpenAiProperties openAiProperties;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String getFeedbackFromGpt(String lectureText) {
        // System 역할 프롬프트 (GPT를 평가자로 설정)
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", """
                너는 초등학생 대상 강의를 평가하는 전문 강의 평가자야.
                이제부터 전달되는 강의 내용을 분석해서, 아래 기준에 따라 피드백을 작성해줘:

                1. 내용 구성력
                2. 전달력 (어휘, 발음, 설명력)
                3. 흥미도
                4. 구체적인 개선점

                최종 출력은 다음 형식을 따라줘:

                - 총평:
                - 구성력:
                - 전달력:
                - 흥미도:
                - 개선점:
                """
        );

        // User 입력 메시지 (강의 텍스트)
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", "다음은 강의 전체 텍스트입니다:\n\n" + lectureText
        );

        // GPT 요청 바디 구성
        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiProperties.getModel());
        body.put("messages", List.of(systemMessage, userMessage));
        body.put("temperature", 0.7);

        // 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // GPT 호출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        // 응답 파싱
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> messageResp = (Map<String, Object>) choices.get(0).get("message");

        return messageResp.get("content").toString().trim();
    }

    /**
     * CoT 생성 템플릿
     * Admin 페이지에서 promptTemplate 전달 받고
     * lectureText, audioInfo, motionInfo 를 따로 전달 받은 뒤
     * 해당 데이터들을 promptTemplate 에 넣어 하나의 String으로 만든다
     */

    public String fillCoTPromptPlaceholders(String promptTemplate, String lectureText, String audioInfo, String motionInfo) {
        return promptTemplate
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo);
    }

    public String getCoT(String prompt, String lectureText, String audioInfo, String motionInfo) {
        String finalPrompt = fillCoTPromptPlaceholders(prompt, lectureText, audioInfo, motionInfo);
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", finalPrompt
        );
        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiProperties.getModel());
        body.put("messages", List.of(systemMessage));
        body.put("temperature", 0.7);

        // 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // GPT 호출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        // 응답 파싱
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> messageResp = (Map<String, Object>) choices.get(0).get("message");

        return messageResp.get("content").toString().trim();
    }

    /**
     * G-EVAL 생성 템플릿
     * Admin 페이지에서 promptTemplate 전달 받고
     * COT, lectureText, audioInfo, motionInfo 를 따로 전달 받은 뒤
     * 해당 데이터들을 promptTemplate 에 넣어 하나의 String으로 만든다
     */

    public String fillGEVALPromptPlaceholders(String promptTemplate, String cot, String lectureText, String audioInfo, String motionInfo) {
        return promptTemplate
                .replace("{CoT}", cot)
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo);
    }

    public String getGEVAL(String prompt, String cot, String lectureText, String audioInfo, String motionInfo) {
        String finalPrompt = fillGEVALPromptPlaceholders(prompt, cot, lectureText, audioInfo, motionInfo);
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", finalPrompt
        );
        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiProperties.getModel());
        body.put("messages", List.of(systemMessage));
        body.put("temperature", 0.7);

        // 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // GPT 호출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        // 응답 파싱
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> messageResp = (Map<String, Object>) choices.get(0).get("message");

        return messageResp.get("content").toString().trim();
    }

    /**
     * Ref-free score 생성 템플릿
     * Admin 페이지에서 promptTemplate 전달 받고
     * gEval, lectureText, audioInfo, motionInfo 를 따로 전달 받은 뒤
     * 해당 데이터들을 promptTemplate 에 넣어 하나의 String으로 만든다
     */

    public String fillRefPromptPlaceholders(String promptTemplate, String gEval, String lectureText, String audioInfo, String motionInfo) {
        return promptTemplate
                .replace("{gEval}", gEval)
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo);
    }

    public String getRef(String prompt, String gEval, String lectureText, String audioInfo, String motionInfo) {
        String finalPrompt = fillRefPromptPlaceholders(prompt, gEval, lectureText, audioInfo, motionInfo);
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", finalPrompt
        );
        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiProperties.getModel());
        body.put("messages", List.of(systemMessage));
        body.put("temperature", 0.7);

        // 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // GPT 호출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        // 응답 파싱
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> messageResp = (Map<String, Object>) choices.get(0).get("message");

        return messageResp.get("content").toString().trim();
    }

    /**
     * Fact Checker
     */

    public String fillFactPromptPlaceholders(String promptTemplate, String reference, String lectureText) {
        return promptTemplate
                .replace("{reference}", reference)
                .replace("{text}", lectureText);
    }

    public String getFact(String prompt, String reference, String lectureText) {
        String finalPrompt = fillFactPromptPlaceholders(prompt, reference, lectureText);
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", finalPrompt
        );
        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiProperties.getModel());
        body.put("messages", List.of(systemMessage));
        body.put("temperature", 0.7);

        // 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiProperties.getKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // GPT 호출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

        // 응답 파싱
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> messageResp = (Map<String, Object>) choices.get(0).get("message");

        return messageResp.get("content").toString().trim();
    }
}
