package com.capstone.backend.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class VocabService {

    private final RestTemplate restTemplate = new RestTemplate();
    //private final String flaskBaseUrl = "http://localhost:5000";  // 실제 서버 환경에 맞게 수정 가능
    private final String flaskBaseUrl = "http://3.39.25.136:5000";

    /**
     * STT 텍스트를 Flask로 보내 어휘 분석 + 난이도 요약을 반환
     */
    public String analyzeVocabulary(String transcript) {
        String url = flaskBaseUrl + "/vocab-check";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("text", transcript);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            Map<String, Object> result = response.getBody();

            if (result == null || result.containsKey("error") ||
                    !result.containsKey("blocked_words") || !result.containsKey("allowed_words")) {
                return "어휘 분석 결과가 올바르지 않습니다.";
            }

            int blockedCount = (int) result.getOrDefault("blocked_count", 0);
            int allowedCount = (int) result.getOrDefault("allowed_count", 0);
            int total = (int) result.getOrDefault("total_tokens", blockedCount + allowedCount);

            String level = String.valueOf(result.getOrDefault("difficulty_level", "분석 불가"));

            List<String> blocked = (List<String>) result.get("blocked_words");
            List<String> allowed = (List<String>) result.get("allowed_words");

            return String.format(
                    "난이도 평가: %s\n총 어휘 수: %d개\n- 초등 금지 어휘: %d개 (%s)\n- 적절한 어휘: %d개 (%s)",
                    level,
                    total,
                    blockedCount,
                    String.join(", ", blocked),
                    allowedCount,
                    String.join(", ", allowed)
            );

        } catch (Exception e) {
            return "어휘 분석 중 예외가 발생했습니다: " + e.getMessage();
        }
    }

    public Map<String, Object> analyzeVocabularyDetail(String transcript) {
        String url = flaskBaseUrl + "/vocab-check";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestBody = Map.of("text", transcript);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            return Map.of("error", "어휘 분석 중 예외 발생: " + e.getMessage());
        }
    }

}
