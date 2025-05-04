package com.capstone.backend.service;

import com.capstone.backend.entity.MotionCaption;
import com.capstone.backend.repository.MotionCaptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MotionCaptionService {

    private final MotionCaptionRepository motionCaptionRepository;

    /**
     * MotionCaption 저장 (기존 데이터 유지, ID 자동 증가 가정 시 null 전달)
     */
    public MotionCaption save(String content) {
        MotionCaption caption = MotionCaption.builder()
                .content(content)
                .build();
        return motionCaptionRepository.save(caption);
    }

    /**
     * 가장 최신 데이터 1개 조회 (ID 기준 내림차순)
     */
    public Optional<MotionCaption> findLatest() {
        return motionCaptionRepository.findAll()
                .stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .findFirst();
    }

    /**
     * 전체 리스트 반환
     */
    public List<MotionCaption> findAll() {
        return motionCaptionRepository.findAll();
    }

    /**
     * 가장 최신 데이터의 content만 반환
     */
    public String getLatestContent() {
        return findLatest().map(MotionCaption::getContent).orElse("");
    }
}
