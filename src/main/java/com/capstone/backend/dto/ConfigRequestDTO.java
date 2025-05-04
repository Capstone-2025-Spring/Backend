package com.capstone.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigRequestDTO {
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
    private String pdf_file;  // 선택 사항이므로 nullable 허용
}
