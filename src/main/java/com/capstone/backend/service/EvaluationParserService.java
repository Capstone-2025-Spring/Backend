package com.capstone.backend.service;

import com.capstone.backend.dto.EvaluationItemDTO;
import com.capstone.backend.dto.EvaluationResultDTO;
import com.capstone.backend.dto.MotionEvaluationDTO;
import com.capstone.backend.dto.MotionEvaluationSplitDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EvaluationParserService {

    public EvaluationResultDTO parse(String rawText) {
        EvaluationResultDTO result = new EvaluationResultDTO();
        List<EvaluationItemDTO> items = new ArrayList<>();

        // ✅ 정규식: 항목명과 점수 (한 줄), 이어지는 이유 한 줄
        Pattern pattern = Pattern.compile(
                "\\s*#{1,}\\s*([^:\\n]+?)\\s*:\\s*(\\d+(?:\\.\\d+)?)\\s*\\n+\\s*#{1,}\\s*(.+?)(?=\\n+\\s*#{1,}[^:\\n]+?\\s*:\\s*\\d+|\\z)",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(rawText);

        while (matcher.find()) {
            String name = matcher.group(1).trim();   // 예: 어휘 수준 평가
            String scoreStr = matcher.group(2).trim();
            String reason = matcher.group(3).trim();

            // 소수점이 있을 수 있으므로 double 처리
            double score = Double.parseDouble(scoreStr);

            if (name.equalsIgnoreCase("Overall Teaching Ability Score")) {
                result.setOverallScore(score);
                result.setOverallReason(reason);
            } else {
                items.add(new EvaluationItemDTO(name, (int) score, reason));
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

    public EvaluationResultDTO parseUserCriteria(String rawText) {
        EvaluationResultDTO result = new EvaluationResultDTO();
        List<EvaluationItemDTO> items = new ArrayList<>();

        // ***** 친절함 : 8
        // @@@@@ 평가 이유 : ...
        Pattern pattern = Pattern.compile(
                "\\*{5}\\s*(.+?)\\s*:\\s*(\\d+)\\s*\\n+@{5}\\s*평가 이유\\s*:\\s*([\\s\\S]*?)(?=\\n\\*{5}|\\z)",
                Pattern.MULTILINE
        );

        Matcher matcher = pattern.matcher(rawText);

        while (matcher.find()) {
            String name = matcher.group(1).trim();
            int score = Integer.parseInt(matcher.group(2).trim());
            String reason = matcher.group(3).trim();

            items.add(new EvaluationItemDTO(name, score, reason));
        }

        result.setCriteriaScores(items);
        return result;
    }

    public MotionEvaluationDTO parseMotionCaptions(String gptResponse) {
        List<MotionEvaluationSplitDTO> results = new ArrayList<>();

        // 모든 유형의 시간 포맷 대응 (매우 느슨한 구조)
        Pattern pattern = Pattern.compile(
                "\\[\\s*(\\d{2})\\s*[:\\s]+(\\d{2})\\s*[:\\s]+(\\d{2})\\s*[:\\s]+(\\d{2})\\s*]\\s*:\\s*(.+?)\\s*\\n+[^@\\n]*@+\\s*이유\\s*[:\\s]+(.+?)(?=(\\n+\\s*\\*|\\z))",
                Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(gptResponse);
        while (matcher.find()) {
            MotionEvaluationSplitDTO dto = new MotionEvaluationSplitDTO(
                    matcher.group(1),  // startMin
                    matcher.group(2),  // startSec
                    matcher.group(3),  // endMin
                    matcher.group(4),  // endSec
                    matcher.group(5).trim(), // label
                    matcher.group(6).trim()  // reason
            );
            results.add(dto);
        }

        return new MotionEvaluationDTO(results);
    }


}
