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
    private String category;
    private String school_level;
    private String subject;
    private String age_group;
    private String class_size;
    private String student_type;
    private List<String> user_criteria;
    private int difficulty;
    private boolean audio_enabled;
    private boolean video_enabled;
    private String pdf_file;
    public String toSummaryString() {
        StringBuilder sb = new StringBuilder();
        sb.append("📌 강의 설정 요약\n");
        sb.append("- 제목: ").append(title).append("\n");
        sb.append("- 카테고리: ").append(category).append("\n");
        sb.append("- 학년: ").append(school_level).append("\n");
        sb.append("- 과목: ").append(subject).append("\n");
        sb.append("- 연령대: ").append(age_group).append("\n");
        sb.append("- 학급 규모: ").append(class_size).append("\n");
        sb.append("- 학생 유형: ").append(student_type).append("\n");
        sb.append("- 난이도: ").append(difficulty).append("\n");
        sb.append("- 오디오 분석 포함 여부: ").append(audio_enabled).append("\n");
        sb.append("- 비디오 분석 포함 여부: ").append(video_enabled).append("\n");

        if (user_criteria != null && !user_criteria.isEmpty()) {
            sb.append("- 사용자 지정 평가 기준: ")
                    .append(String.join(", ", user_criteria)).append("\n");
        }

        return sb.toString();
    }
}
