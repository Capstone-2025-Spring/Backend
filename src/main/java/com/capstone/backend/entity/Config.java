package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // 강의 대상 그룹 (예: 초등 고학년, 중학생 등)
    private String audienceGroup;

    // 강의 대상 유형 (예: 중간 수준, 고학력자 등)
    private String audienceType;

    private String subject;

    // 사용자 지정 평가 기준 목록을 comma-separated string으로 저장
    @ElementCollection
    private List<String> userCriteria;

    private boolean audioEnabled;
    private boolean videoEnabled;
}
