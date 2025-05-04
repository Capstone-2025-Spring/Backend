package com.capstone.backend.dto;

import com.capstone.backend.entity.*;

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

            List<PoseLandmark> landmarks = frameDto.getResults().stream().map(landmarkDto -> {
                PoseLandmark lm = new PoseLandmark();
                lm.setX(landmarkDto.getX());
                lm.setY(landmarkDto.getY());
                lm.setZ(landmarkDto.getZ());
                lm.setVisibility(landmarkDto.getVisibility());
                lm.setPoseFrame(frame);
                return lm;
            }).collect(Collectors.toList());

            frame.setResults(landmarks);
            return frame;
        }).collect(Collectors.toList());

        holistic.setPoseFrames(frames);
        return holistic;
    }
}