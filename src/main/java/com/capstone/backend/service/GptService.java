package com.capstone.backend.service;

import com.capstone.backend.config.OpenAiProperties;
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

    private String promptTemplateCoT = """
            당신은 교육 분야의 AI 평가 전문가입니다.
                                          
            다음은 하나의 강의 장면에서 수집된 멀티모달 데이터입니다:
            - 강의 텍스트(STT 변환): {text}
            - 음성 분석 결과(속도, 억양, 강조 등): {audio}
            - 제스처 및 표정 등 모션 분석 요약: {motion}
                                          
            당신의 과제는 이 데이터를 기반으로, **강사의 언어적 전달력**을 평가하기 위한 **Chain-of-Thought 방식의 평가 절차(가이드라인)**를 단계별로 설계하는 것입니다.
                                          
            여기서 "언어적 전달력"이란 다음 요소들을 포함할 수 있습니다:
            - 어휘의 적절성
            - 문장 구조의 명확성
            - 표현 방식의 다양성
            - 억양, 강세 등 음성적 요소의 전달력
            - 모션/표정과의 연계성
            - 청중과의 상호작용을 유도하는 말투/어조
            - {criteria}
                                          
            평가는 다음 조건을 충족해야 합니다:
            - **텍스트, 음성, 모션** 정보를 균형 있게 분석할 것
            - **객관적이고 반복 가능한 평가 절차**로 구성할 것
            - 실제 교육자 피드백에 적용 가능한 **실용적 단계**일 것
                                          
            출력 형식 예시:
            1. STT 텍스트에서 핵심 주제 및 주요 메시지를 파악한다.
            2. 어휘 및 표현 방식이 교육 목적과 일치하는지 평가한다.
            3. 문장의 구조가 명확하고 청중이 이해하기 쉬운지를 검토한다.
            4. 발화의 억양과 속도가 전달 의도와 일치하는지를 평가한다.
            5. 제스처 및 표정이 핵심 메시지를 보완하는지 확인한다.
            6. 전체 커뮤니케이션이 청중과의 연결을 강화하는 방식인지 판단한다.
            ...
                                          
            이제 위 데이터를 기반으로, 당신만의 평가 절차를 논리적 순서로 단계별로 제시해 주세요.
            """;
    private String promptTemplateGEval = """
            당신에게는 모의 강의에서 나온 데이터가 주어집니다          
            당신의 과제는 강의에서 사용된 **어휘 선택(word choice)**을 1점에서 10점 사이로 평가하는 것입니다.  
            단어 선택이 학습자에게 명확하고, 맥락에 적절하며, 다양하고, 흥미롭게 느껴지는지를 고려해주세요.
             
            평가 절차 (LLM 생성): {CoT}
            
            모의 강의 데이터
            - 강의 Speech-To-Text 전문: {text}
            - 모션 동작 데이터: {motion}
            - 음성 분석 데이터: {audio} 
            
            평가 기준:
            어휘의 명확성
            맥락에 맞는 적절성           
            어휘의 다양성   
            학습자 흥미 유발
            {criteria}
            
            평가 양식 (각 항목에 대해 1-10점의 점수 부여 및 1~2줄의 설명 작성):    
            어휘 명확성
            맥락에 맞는 적절성
            어휘의 다양성
            학습자 흥미 유발
            {criteria} 
            """;
    private String promptTemplateRef = """
            다음은 한 강의 장면에 대한 평가자 에이전트의 평가 결과입니다.
            입력은 음성, 움직임, 언어 등의 요소로 구성된 멀티모달 데이터입니다.
            각 항목의 점수와 그에 대한 이유가 포함되어 있습니다.
                        
            당신의 역할은 **메타 평가자(Meta-Evaluator, SAGE Agent)**입니다.
            아래 내용을 기반으로 다음을 수행해 주세요
                        
            ---
                        
            **[1] 입력 데이터 (Hypothesis)**
            - 텍스트 요약: {text}
            - 음성 분석 결과 요약: {audio}
            - Holistic 움직임 분석 요약: {motion}
                        
            ---
                        
            **[2] 평가자 에이전트의 평가 결과 (1차 평가)**
            {gEval}
            ---
                        
            **[당신의 과제]**
                        
            1. 위 평가 결과에서 **각 항목의 점수가 입력에 비추어 적절한지 판단**하고,
               필요한 경우 점수를 수정하고 그 **이유를 간단히 서술**해 주세요.
                        
            2. 평가 기준이 **모호하거나 애매하다면**, 해당 기준의 정의를 **개선 제안**해 주세요.
                        
            3. 기존 평가 기준만으로 **충분하지 않다고 판단될 경우**,
               추가로 포함하면 좋을 **새로운 평가 기준**을 제안해 주세요.
               (예: 감정 표현력, 인터랙션 밀도 등)
                        
            """;
    private String promptTemplateFact = """
            당신은 전문가 수준의 팩트체커입니다. 아래 두 가지 텍스트가 주어집니다:
                        
            1. Reference (참조): 사실에 기반한 원문 (강의 교안)
            2. Hypothesis (가설): 생성된 강의 내용 텍스트 (강사의 발화 텍스트, STT 기반)
                        
            당신의 과제는 다음 평가 항목에 대해 1점에서 5점까지 점수를 매기고, 각 항목별로 간단한 이유(설명)를 작성하는 것입니다:
                        
            1. 사실 일치도 (Factual Consistency): 가설의 정보가 참조 문서의 내용과 일치하는가?
            2. 형용사 사용의 적절성 (Adjective Regularity): 형용사가 과장되지 않고 적절하게 사용되었는가?
            3. 배경 지식의 일치도 (Knowledge Congruence): 참조 문서에 없는 외부 정보가 삽입되었는가?
            4. 문체 일관성 (Style Alignment): 참조 문서와 문체나 어조가 유사한가?
                        
            점수 기준 (1~5점):
            - 1점: 매우 부정확함
            - 2점: 부정확함
            - 3점: 중립적임
            - 4점: 대체로 정확함
            - 5점: 매우 정확함
                        
            출력 형식 예시:
                        
            사실 일치도: [점수]
            설명: [이유]
                        
            형용사 사용의 적절성: [점수]
            설명: [이유]
                        
            배경 지식의 일치도: [점수]
            설명: [이유]
                        
            문체 일관성: [점수]
            설명: [이유]
            """;

    public String runFullEvaluationPipeline(String lectureText, String audioInfo, String motionInfo, String criteriaCoT, String criteriaGEval) {
        // Step 1: CoT 생성
        String cot = getCoT(lectureText, audioInfo, motionInfo, criteriaCoT);
        System.out.println("[1단계 - CoT 전문]\n" + cot);

        // Step 2: GEval 생성
        String gEval = getGEval(cot, lectureText, audioInfo, motionInfo, criteriaGEval);
        System.out.println("[2단계 - GEval 점수 및 설명]\n" + gEval);

        // Step 3: Ref 평가 (Meta-Evaluator)
        String finalRefinedEvaluation = getRef(gEval, lectureText, audioInfo, motionInfo);
        System.out.println("[3단계 - Meta 평가 결과]\n" + finalRefinedEvaluation);

        return cot + "\n\n" + gEval + "\n\n" + finalRefinedEvaluation;
    }


    public String fillCoTPromptPlaceholders(String lectureText, String audioInfo, String motionInfo, String criteria) {
        PromptTemplate template = promptTemplateService.getByType("CoT");

        return template.getContent()
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo)
                .replace("{criteria}", criteria);
    }

    public String getCoT(String lectureText, String audioInfo, String motionInfo, String criteria) {
        String finalPrompt = fillCoTPromptPlaceholders(lectureText, audioInfo, motionInfo, criteria);
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

    public String fillGEvalPromptPlaceholders(String cot, String lectureText, String audioInfo, String motionInfo, String criteria) {
        PromptTemplate template = promptTemplateService.getByType("GEval");
        return template.getContent()
                .replace("{CoT}", cot)
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo)
                .replace("{criteria}", criteria);
    }

    public String getGEval(String cot, String lectureText, String audioInfo, String motionInfo, String criteria) {
        String finalPrompt = fillGEvalPromptPlaceholders(cot, lectureText, audioInfo, motionInfo, criteria);
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

    public String fillRefPromptPlaceholders(String gEval, String lectureText, String audioInfo, String motionInfo) {
        PromptTemplate template = promptTemplateService.getByType("Ref");
        return template.getContent()
                .replace("{gEval}", gEval)
                .replace("{text}", lectureText)
                .replace("{audio}", audioInfo)
                .replace("{motion}", motionInfo);
    }

    public String getRef(String gEval, String lectureText, String audioInfo, String motionInfo) {
        String finalPrompt = fillRefPromptPlaceholders(gEval, lectureText, audioInfo, motionInfo);
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
