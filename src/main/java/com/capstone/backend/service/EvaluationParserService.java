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

        // ✅ 항목 패턴: 점수와 이유가 각각 한 줄씩
        Pattern pattern = Pattern.compile(
                "\\s*#{1,}\\s*(.+?)\\s*:\\s*(\\d+)\\s*\\n+\\s*#{1,}\\s*(.+?)\\s*(?=\\n+\\s*#{1,}|\\z)",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(rawText);

        while (matcher.find()) {
            String name = matcher.group(1).trim();   // 예: 어휘 수준 평가
            int score = Integer.parseInt(matcher.group(2).trim());
            String reason = matcher.group(3).trim(); // 한 줄 이유

            if (name.equalsIgnoreCase("Overall Teaching Ability Score")) {
                result.setOverallScore(score);
                result.setOverallReason(reason);
            } else {
                items.add(new EvaluationItemDTO(name, score, reason));
            }
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
