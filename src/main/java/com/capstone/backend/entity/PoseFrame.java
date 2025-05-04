package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "pose_frame")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoseFrame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holistic_id")
    private Holistic holistic;

    @OneToMany(mappedBy = "poseFrame", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PoseLandmark> results;
}
