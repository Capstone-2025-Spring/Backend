package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String type; // CoT, GEval, Ref, Fact

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

}

