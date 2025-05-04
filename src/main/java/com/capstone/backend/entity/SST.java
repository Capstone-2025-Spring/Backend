package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SST {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ✅ ID 자동 생성
    private Long id;

    @Lob  // 긴 문자열 저장 가능
    @Column(columnDefinition = "TEXT")
    private String content;
}
