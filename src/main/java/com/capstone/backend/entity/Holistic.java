package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "holistic")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Holistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoId;

    @OneToMany(mappedBy = "holistic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PoseFrame> poseFrames;
}
