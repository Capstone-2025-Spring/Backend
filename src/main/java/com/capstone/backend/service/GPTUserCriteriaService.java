package com.capstone.backend.service;

import com.capstone.backend.config.OpenAiProperties;
import com.capstone.backend.dto.ConfigRequestDTO;
import com.capstone.backend.dto.EvaluationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GPTUserCriteriaService {

    private final OpenAiProperties openAiProperties;
    private final EvaluationParserService evaluationParserService;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final String promptTemplate = """
        너는 교육 전문가로서 사용자 정의 기준에 따라 강의를 평가하는 역할을 맡고 있다.

        아래는 강의 요약, 강사의 언어적 설명, 비언어적 표현이며,
        사용자가 직접 정의한 평가 기준도 함께 제시된다.
        이 기준에 따라 강의를 종합적으로 평가하고, 그 이유를 두 문장 이상으로 자세히 설명하라.

        ---
        [사용자 정의 평가 기준]
        {criteria}

        [강의 정보 요약]
        {config}

        [강사의 언어적 설명 요약]
        {text}

        [강사의 비언어적 행동 요약]
        {motion}

        ---
        평가 결과:
        ***** 사용자 정의 평가 기준 : 점수 (1~10 사이 정수)
        @@@@@ 평가 이유 : 두 문장 이상의 평가 이유
        
        예시
        ***** 친절함 : 9
        @@@@@ 평가 이유 : 끊임 없이 강의 대상의 이해도를 측정하려 노력했다. 어투도 부드러웠다.
        """;

    public String fillPrompt(ConfigRequestDTO config, String lectureText, String motionInfo) {
        String criteriaText = (config.getUser_criteria() != null && !config.getUser_criteria().isEmpty())
                ? String.join(", ", config.getUser_criteria())
                : "제공된 사용자 기준 없음";

        return promptTemplate
                .replace("{criteria}", criteriaText)
                .replace("{config}", config.toSummaryString())
                .replace("{text}", lectureText)
                .replace("{motion}", motionInfo);
    }

    public EvaluationResultDTO getCustomEvaluation(ConfigRequestDTO config, String lectureText, String motionInfo) {
        String prompt = fillPrompt(config, lectureText, motionInfo);

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

        // 🔽 GPT 결과를 파싱하여 DTO로 변환
        return evaluationParserService.parseUserCriteria(gptResponse);
    }

}
