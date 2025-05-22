package com.capstone.backend.dto;

import lombok.Data;

@Data
public class EventInfoDTO {
    private int start_ms;
    private int end_ms;
    private String description;
}
