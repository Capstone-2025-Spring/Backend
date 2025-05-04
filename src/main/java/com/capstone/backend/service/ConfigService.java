package com.capstone.backend.service;

import com.capstone.backend.dto.ConfigRequestDTO;
import com.capstone.backend.entity.Config;
import com.capstone.backend.entity.Criteria;
import com.capstone.backend.repository.ConfigRepository;
import com.capstone.backend.repository.CriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;
    private final CriteriaRepository criteriaRepository;

    public Config save(ConfigRequestDTO dto) {

        // user_criteria 저장
        if (dto.getUser_criteria() != null) {
            for (String content : dto.getUser_criteria()) {
                if (!criteriaRepository.existsByTypeAndContent("User", content)) {
                    criteriaRepository.save(Criteria.builder()
                            .type("User")
                            .content(content)
                            .build());
                }
            }
        }

        // Config 저장
        Config config = Config.builder()
                .title(dto.getTitle())
                .category(dto.getCategory())
                .schoolLevel(dto.getSchool_level())
                .subject(dto.getSubject())
                .ageGroup(dto.getAge_group())
                .classSize(dto.getClass_size())
                .studentType(dto.getStudent_type())
                .difficulty(dto.getDifficulty())
                .audioEnabled(dto.isAudio_enabled())
                .videoEnabled(dto.isVideo_enabled())
                .pdfFile(dto.getPdf_file())
                .build();

        return configRepository.save(config);
    }
}
