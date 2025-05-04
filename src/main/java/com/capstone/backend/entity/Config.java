package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    private String category;

    private String schoolLevel;
    private String subject;
    private String ageGroup;
    private String classSize;
    private String studentType;

    private int difficulty;

    private boolean audioEnabled;
    private boolean videoEnabled;

    private String pdfFile;  // DB에 null 저장 허용
}
