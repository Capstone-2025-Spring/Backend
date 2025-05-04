package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotionCaption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Lob  // 긴 텍스트 저장 가능
    @Column(columnDefinition = "TEXT")
    private String content;
}
