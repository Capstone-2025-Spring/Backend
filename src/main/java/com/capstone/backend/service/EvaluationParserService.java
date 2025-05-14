package com.capstone.backend.service;

import com.capstone.backend.dto.EvaluationItemDTO;
import com.capstone.backend.dto.EvaluationResultDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EvaluationParserService {

    public EvaluationResultDTO parse(String rawText) {
        EvaluationResultDTO result = new EvaluationResultDTO();
        List<EvaluationItemDTO> items = new ArrayList<>();

        // 전체 점수 추출
        Pattern overallScorePattern = Pattern.compile("Overall Teaching Ability Score\\s*:\\s*(\\d+(\\.\\d+)?)");
        Matcher overallScoreMatcher = overallScorePattern.matcher(rawText);
        if (overallScoreMatcher.find()) {
            result.setOverallScore(Double.parseDouble(overallScoreMatcher.group(1)));
        }

        // 전체 종합 평가 설명 추출
        Pattern overallReasonPattern = Pattern.compile("Overall Teaching Ability Score\\s*:\\s*\\d+(\\.\\d+)?\\s*@@@@@ 해당 점수를 부여한 이유 ?:\\s*([\\s\\S]*?)$");
        Matcher overallReasonMatcher = overallReasonPattern.matcher(rawText);
        if (overallReasonMatcher.find()) {
            result.setOverallReason(overallReasonMatcher.group(2).trim());
        }

        // 평가 항목 추출 (🔹 라인도 종료 조건에 추가)
        Pattern itemPattern = Pattern.compile(
                "#####\\s*(.*?)\\s*:\\s*(\\d+)\\s*@@@@@ 해당 점수를 부여한 이유 ?:\\s*([\\s\\S]*?)(?=#####|Overall Teaching Ability Score|###|🔹|$)"
        );

        Matcher itemMatcher = itemPattern.matcher(rawText);

        while (itemMatcher.find()) {
            String name = itemMatcher.group(1).trim();
            int score = Integer.parseInt(itemMatcher.group(2).trim());
            String reason = itemMatcher.group(3).trim();
            items.add(new EvaluationItemDTO(name, score, reason));
        }

        result.setCriteriaScores(items);
        return result;
    }

    public EvaluationResultDTO parseWithEvent(String eventText) {
        EvaluationResultDTO result = new EvaluationResultDTO();

        // 이벤트 점수: ***** 점수 : 8
        Pattern scorePattern = Pattern.compile("\\*{5}\\s*점수\\s*:\\s*(\\d+)");
        Matcher scoreMatcher = scorePattern.matcher(eventText);
        if (scoreMatcher.find()) {
            result.setEventScore(scoreMatcher.group(1));
        }

        // 이벤트 이유: @@@@@ 평가 이유 : ...
        Pattern reasonPattern = Pattern.compile("@{5}\\s*평가 이유\\s*:\\s*([\\s\\S]+)");
        Matcher reasonMatcher = reasonPattern.matcher(eventText);
        if (reasonMatcher.find()) {
            result.setEventReason(reasonMatcher.group(1).trim());
        }

        return result;
    }
}
