package com.capstone.backend.service;

import com.capstone.backend.config.OpenAiProperties;
import com.capstone.backend.dto.EvaluationResultDTO;
import com.capstone.backend.entity.PromptTemplate;
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

    private final PromptTemplateService promptTemplateService;
    private final EvaluationParserService evaluationParserService;

    public EvaluationResultDTO runFullEvaluationPipeline(String lectureText, String audioInfo, String motionInfo, String configInfo, String criteriaCoT, String criteriaGEval) {
        long totalStart = System.currentTimeMillis();

        // Step 1: CoT
        long cotStart = System.currentTimeMillis();
        String cot = getCoT(lectureText, audioInfo, motionInfo, configInfo, criteriaCoT);
        long cotEnd = System.currentTimeMillis();
        System.out.println("[1단계 - CoT 전문]\n" + cot);
        System.out.println("⏱️ CoT 생성 소요 시간: " + (cotEnd - cotStart) + "ms");

        // Step 2: GEval
        long gEvalStart = System.currentTimeMillis();
        String gEval = getGEval(cot, lectureText, audioInfo, motionInfo, configInfo, criteriaGEval);
        long gEvalEnd = System.currentTimeMillis();
        System.out.println("[2단계 - GEval 점수 및 설명]\n" + gEval);
        System.out.println("⏱️ GEval 생성 소요 시간: " + (gEvalEnd - gEvalStart) + "ms");

        // Step 3: SAGEval (Meta Evaluation)
        long sageEvalStart = System.currentTimeMillis();
        String SAGEval = getSAGEval(gEval, lectureText, audioInfo, configInfo, motionInfo);
        long sageEvalEnd = System.currentTimeMillis();
        System.out.println("[3단계 - Meta 평가 결과]\n" + SAGEval);
        System.out.println("⏱️ SAGEval 생성 소요 시간: " + (sageEvalEnd - sageEvalStart) + "ms");

        long totalEnd = System.currentTimeMillis();
        System.out.println("🧾 전체 파이프라인 소요 시간: " + (totalEnd - totalStart) + "ms");

        return evaluationParserService.parse(SAGEval);
    }


    public String fillCoTPromptPlaceholders(String lectureText, String audioInfo, String motionInfo, String configInfo, String criteria) {
        PromptTemplate template = promptTemplateService.getByType("CoT");

        return template.getContent()
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo)
                .replace("{config}", configInfo)
                .replace("{criteria}", criteria);
    }

    public String getCoT(String lectureText, String audioInfo, String motionInfo, String configInfo, String criteria) {
        return fillCoTPromptPlaceholders(lectureText, audioInfo, motionInfo, configInfo, criteria);
    }

    public String fillGEvalPromptPlaceholders(String cot, String lectureText, String audioInfo, String motionInfo, String configInfo, String criteria) {
        PromptTemplate template = promptTemplateService.getByType("GEval");
        return template.getContent()
                .replace("{CoT}", cot)
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo)
                .replace("{config}",configInfo)
                .replace("{criteria}", criteria);
    }

    public String getGEval(String cot, String lectureText, String audioInfo, String motionInfo, String configInfo, String criteria) {
        String finalPrompt = fillGEvalPromptPlaceholders(cot, lectureText, audioInfo, motionInfo, configInfo, criteria);
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

    public String fillSAGEvalPromptPlaceholders(String gEval, String lectureText, String audioInfo, String configInfo, String motionInfo) {
        PromptTemplate template = promptTemplateService.getByType("SAGEval");
        return template.getContent()
                .replace("{gEval}", gEval)
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo)
                .replace("{config}", configInfo);
    }

    public String getSAGEval(String gEval, String lectureText, String audioInfo, String configInfo, String motionInfo) {
        String finalPrompt = fillSAGEvalPromptPlaceholders(gEval, lectureText, audioInfo, configInfo, motionInfo);
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

    public String getFact(String lectureText) {
        PromptTemplate template = promptTemplateService.getByType("Fact");

        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", template.getContent()
        );

        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", "\n다음은 강의 전체 텍스트입니다:\n\n" + lectureText
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
