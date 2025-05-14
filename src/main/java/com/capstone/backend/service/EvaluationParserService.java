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

        // ✅ Overall Score ( # 1개 이상 허용 )
        Pattern overallScorePattern = Pattern.compile("#{1,}\\s*Overall Teaching Ability Score\\s*:\\s*(\\d+)");
        Matcher overallScoreMatcher = overallScorePattern.matcher(rawText);
        if (overallScoreMatcher.find()) {
            result.setOverallScore(Double.parseDouble(overallScoreMatcher.group(1)));
        }

        // ✅ Overall Reason ( #/@ 1개 이상 허용 )
        Pattern overallReasonPattern = Pattern.compile(
                "#{1,}\\s*Overall Teaching Ability Score\\s*:\\s*\\d+\\s*\\n@{1,}\\s+([\\s\\S]*?)$",
                Pattern.MULTILINE
        );
        Matcher overallReasonMatcher = overallReasonPattern.matcher(rawText);
        if (overallReasonMatcher.find()) {
            result.setOverallReason(overallReasonMatcher.group(1).trim());
        }

        // ✅ 항목별 점수 + 이유 ( #/@ 1개 이상, 대괄호 유무 허용 )
        Pattern itemPattern = Pattern.compile(
                "#{1,}\\s*(?:\\[(.+?)\\]|(.+?))\\s*:\\s*(\\d+)\\s*\\n@{1,}\\s+([\\s\\S]*?)(?=#{1,}\\s*(?:\\[.+?\\]|Overall Teaching Ability Score|.+?)\\s*:\\s*\\d+|\\z)",
                Pattern.MULTILINE
        );
        Matcher itemMatcher = itemPattern.matcher(rawText);

        while (itemMatcher.find()) {
            String name = itemMatcher.group(1) != null ? itemMatcher.group(1).trim() : itemMatcher.group(2).trim();
            int score = Integer.parseInt(itemMatcher.group(3).trim());
            String reason = itemMatcher.group(4).trim();
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
