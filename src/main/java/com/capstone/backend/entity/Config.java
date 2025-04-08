package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Config {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String schoolLevel;
    private String subject;
    private String age;
    private String classSize;
    private String studentType;
    private boolean audioEnabled;
    private boolean videoEnabled;
}
