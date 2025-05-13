package com.capstone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigRequestDTO {
    private String title;
    private String audience_group;
    private String audience_type;
    private String subject;
    private List<String> user_criteria;
    private boolean audio_enabled;
    private boolean video_enabled;
    public String toSummaryString() {
        StringBuilder sb = new StringBuilder();
        sb.append("📌 강의 설정 요약\n");
        sb.append("- 제목: ").append(title).append("\n");
        sb.append("- 강의 대상 그룹: ").append(audience_group).append("\n");
        sb.append("- 강의 대상 유형: ").append(audience_type).append("\n");
        sb.append("- 과목: ").append(subject).append("\n");
        sb.append("- 오디오 분석 포함 여부: ").append(audio_enabled).append("\n");
        sb.append("- 비디오 분석 포함 여부: ").append(video_enabled).append("\n");
        if (user_criteria != null && !user_criteria.isEmpty()) {
            sb.append("- 사용자 지정 평가 기준 (선택): ")
                    .append(String.join(", ", user_criteria)).append("\n");
        }

        return sb.toString();
    }
}
