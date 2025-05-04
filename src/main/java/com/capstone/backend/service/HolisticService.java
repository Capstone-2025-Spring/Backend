package com.capstone.backend.service;

import com.capstone.backend.dto.HolisticConverter;
import com.capstone.backend.dto.HolisticDataDTO;
import com.capstone.backend.dto.PoseFrameDTO;
import com.capstone.backend.dto.PoseLandmarkDTO;
import com.capstone.backend.entity.Holistic;
import com.capstone.backend.repository.HolisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<Holistic> findLatest() {
        return holisticRepository.findAll()
                .stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .findFirst();
    }

    public Optional<HolisticDataDTO> findLatestAsDTO() {
        return holisticRepository.findAll()
                .stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .findFirst()
                .map(this::toDTO);
    }

    public List<HolisticDataDTO> findAllAsDTO() {
        return holisticRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<HolisticDataDTO> findByVideoIdAsDTO(String videoId) {
        return Optional.ofNullable(holisticRepository.findByVideoId(videoId))
                .map(this::toDTO);
    }

    private HolisticDataDTO toDTO(Holistic entity) {
        List<PoseFrameDTO> frames = entity.getPoseFrames().stream()
                .map(frame -> new PoseFrameDTO(
                        frame.getTimestamp(),
                        frame.getResults().stream()
                                .map(l -> new PoseLandmarkDTO(
                                        l.getX(), l.getY(), l.getZ(), l.getVisibility()
                                )).toList()
                )).toList();

        return new HolisticDataDTO(entity.getVideoId(), frames);
    }
}
