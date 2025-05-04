package com.capstone.backend.service;

import com.capstone.backend.dto.HolisticConverter;
import com.capstone.backend.dto.HolisticDataDTO;
import com.capstone.backend.entity.Holistic;
import com.capstone.backend.repository.HolisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HolisticService {

    private final HolisticRepository holisticRepository;

    /**
     * 비디오 ID 중복 여부 확인
     */
    public boolean existsByVideoId(String videoId) {
        return holisticRepository.existsByVideoId(videoId);
    }

    /**
     * 데이터 저장
     */
    public Holistic save(HolisticDataDTO dto) {
        Holistic entity = HolisticConverter.toEntity(dto);
        return holisticRepository.save(entity);
    }

    /**
     * videoId로 조회
     */
    public Optional<Holistic> findByVideoId(String videoId) {
        return Optional.ofNullable(holisticRepository.findByVideoId(videoId));
    }
}
