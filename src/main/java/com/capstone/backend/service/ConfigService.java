package com.capstone.backend.service;

import com.capstone.backend.dto.ConfigRequestDTO;
import com.capstone.backend.entity.Config;
import com.capstone.backend.entity.Criteria;
import com.capstone.backend.repository.ConfigRepository;
import com.capstone.backend.repository.CriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;
    private final CriteriaRepository criteriaRepository;

    /**
     * 새 Config 저장 (기존 Config 유지)
     */
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

        // 새 Config 저장
        Config config = Config.builder()
                .title(dto.getTitle())
                .audienceGroup(dto.getAudience_group())
                .audienceType(dto.getAudience_type())
                .subject(dto.getSubject())
                .userCriteria(dto.getUser_criteria())
                .audioEnabled(dto.isAudio_enabled())
                .videoEnabled(dto.isVideo_enabled())
                .build();

        return configRepository.save(config);
    }


    /**
     * 가장 최근 Config 1개 반환
     */
    public Optional<Config> findLatest() {
        return configRepository.findAll()
                .stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId())) // id 기준 내림차순
                .findFirst();
    }

    /**
     * 가장 최근 Config → DTO 반환
     */
    public Optional<ConfigRequestDTO> findLatestAsDTO() {
        return findLatest().map(this::toDTO);
    }

    /**
     * 전체 Config 리스트 조회
     */
    public List<ConfigRequestDTO> findAllAsDTO() {
        return configRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 특정 ID로 Config 조회
     */
    public Optional<ConfigRequestDTO> findByIdAsDTO(Long id) {
        return configRepository.findById(id).map(this::toDTO);
    }

    /**
     * 내부 변환 함수 (Entity → DTO)
     */
    private ConfigRequestDTO toDTO(Config config) {
        return new ConfigRequestDTO(
                config.getTitle(),
                config.getAudienceGroup(),
                config.getAudienceType(),
                config.getSubject(),
                config.getUserCriteria(), // 실제 리스트로 저장된 경우 그대로 사용
                config.isAudioEnabled(),
                config.isVideoEnabled()
        );
    }

}
