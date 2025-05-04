package com.capstone.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pose_landmark")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoseLandmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double x;
    private double y;
    private double z;
    private double visibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pose_frame_id")
    @JsonIgnore
    private PoseFrame poseFrame;
}
