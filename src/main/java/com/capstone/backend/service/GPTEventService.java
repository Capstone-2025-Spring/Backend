package com.capstone.backend.service;

import com.capstone.backend.config.OpenAiProperties;
import com.capstone.backend.dto.EvaluationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GPTEventService {
    private final OpenAiProperties openAiProperties;
    private final EvaluationParserService evaluationParserService;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final String promptTemplate = """
        너는 교육 전문가로서 교사의 강의력, 특히 수업 중 발생하는 **이벤트 상황 대응 능력**을 평가하는 역할을 맡고 있다.

        다음은 수업 중 실제로 발생한 돌발 상황과, 그에 대한 교사의 대처 및 비언어적 표현이다.
        아래 항목을 종합적으로 고려하여 교사의 대응이 적절했는지 평가하고, 그 이유를 설명해줘.
        마지막에는 개선할 점이 있다면 간단히 제시해줘.

        ---

        📌 [상황 설명] : {event}

        📌 [강의 정보] : {config}

        🧍‍♀️ [강사의 언어적 대처] : {text}

        🗣️ [강사의 비언어적 행동] : {motion}
        ( ※ 모션 데이터 형식: [start_mm:start_ss : end_mm:end_ss] : label
          ※ label_map = {
              0: "서있음",
              1: "손 머리에 대는 중",
              2: "뒤 돌고 있음",
              3: "팔짱끼는 중"
              4: "고개를 숙이고 있음"
          })

        ---

        🎯 평가 기준 (다음 중 하나라도 고려해서 평가해도 좋아)
        - 교사의 감정 조절 능력
        - 학생의 주의를 환기시키는 능력
        - 질서 회복을 위한 말과 행동의 효과성
        - 비언어적 표현의 설득력

        ---

        ✍️ 평가 결과:
        (여기에 GPT가 판단하고, 이유와 함께 피드백을 적게 함. 1-10까지의 점수를 평가해야 함. 피드백은 반드시 두 줄 이상으로 구성해야 함)
        결과 형식은 반드시 다음과 같은 형식으로 제공되어야 함.
        ***** 점수 : [점수]
        @@@@@ 평가 이유 : [평가 이유]
        """;

    public String fillEventPrompt(String eventInfo, String lectureText, String motionInfo, String configInfo) {
        return promptTemplate
                .replace("{event}", eventInfo)
                .replace("{text}", lectureText)
                .replace("{motion}", motionInfo)
                .replace("{config}", configInfo);
    }

    public EvaluationResultDTO getEventEvaluation(String eventInfo, String lectureText, String motionInfo, String configInfo) {
        String prompt = fillEventPrompt(eventInfo, lectureText, motionInfo, configInfo);

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

        // 🔽 이벤트 평가 전용 파서 호출
        return evaluationParserService.parseWithEvent(gptResponse);
    }

}
