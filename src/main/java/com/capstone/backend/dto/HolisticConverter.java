package com.capstone.backend.dto;

import com.capstone.backend.entity.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HolisticConverter {

    public static Holistic toEntity(HolisticDataDTO dto) {
        Holistic holistic = new Holistic();
        holistic.setVideoId(dto.getVideoId());

        List<PoseFrame> frames = dto.getHolisticData().stream().map(frameDto -> {
            PoseFrame frame = new PoseFrame();
            frame.setTimestamp(frameDto.getTimestamp());
            frame.setHolistic(holistic);

            // ✅ null 방어: results가 null이면 빈 리스트로 처리
            List<PoseLandmark> landmarks = (frameDto.getResults() != null)
                    ? frameDto.getResults().stream().map(landmarkDto -> {
                PoseLandmark lm = new PoseLandmark();
                lm.setX(landmarkDto.getX());
                lm.setY(landmarkDto.getY());
                lm.setZ(landmarkDto.getZ());
                lm.setVisibility(landmarkDto.getVisibility());
                lm.setPoseFrame(frame);
                return lm;
            }).collect(Collectors.toList())
                    : Collections.emptyList();

            frame.setResults(landmarks);
            return frame;
        }).collect(Collectors.toList());

        holistic.setPoseFrames(frames);
        return holistic;
    }
}
