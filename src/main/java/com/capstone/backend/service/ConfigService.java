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

        Config config = Config.builder()
                .title(dto.getTitle())
                .schoolLevel(dto.getSchool_level())
                .subject(dto.getSubject())
                .age(dto.getAge())
                .classSize(dto.getClass_size())
                .studentType(dto.getStudent_type())
                .audioEnabled(dto.isAudio_enabled())
                .videoEnabled(dto.isVideo_enabled())
                .build();

        return configRepository.save(config);
    }
}
