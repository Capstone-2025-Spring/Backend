package com.capstone.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCaption {
    @Id
    private Long id;

    @Column
    private String type;

    @Column
    private String startTime;
    @Column
    private String endTime;

    @Column
    private String content;
}
