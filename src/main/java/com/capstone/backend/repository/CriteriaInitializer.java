package com.capstone.backend.repository;

import com.capstone.backend.entity.Criteria;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CriteriaInitializer {

    private final CriteriaRepository criteriaRepository;
    @PostConstruct
    public void initCriteria() {
        Map<String, List<String>> criteriaMap = Map.of(
                "CoT", List.of(
                        "명확한 어휘 선택",
                        "문장 구조, 구성의 명확성",
                        "맥락에 맞는 적절성",
                        "음성적 요소의 전달력",
                        "중요 포인트에 적절한 강조",
                        "청중과의 상호작용 유도하는 말투와 어조"
                ),
                "GEval", List.of(
                        "어휘의 명확성",
                        "맥락에 맞는 적절성",
                        "문장 구성, 구조의 적절성",
                        "청중과의 상호작용",
                        "음성적 요소의 전달력",
                        "비언어적 표현의 적절성"
                ),
                "Fact", List.of(
                        "사실 일치도 (Factual Consistency): 가설의 정보가 참조 문서의 내용과 일치하는가?",
                        "형용사 사용의 적절성 (Adjective Regularity): 형용사가 과장되지 않고 적절하게 사용되었는가?",
                        "배경 지식의 일치도 (Knowledge Congruence): 참조 문서에 없는 외부 정보가 삽입되었는가?",
                        "문체 일관성 (Style Alignment): 참조 문서와 문체나 어조가 유사한가?"
                )
        );

        for (Map.Entry<String, List<String>> entry : criteriaMap.entrySet()) {
            String type = entry.getKey();
            for (String content : entry.getValue()) {
                if (!criteriaRepository.existsByTypeAndContent(type, content)) {
                    criteriaRepository.save(Criteria.builder()
                            .type(type)
                            .content(content)
                            .build());
                }
            }
        }
    }
}
