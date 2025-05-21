package com.capstone.backend.controller;

import com.capstone.backend.dto.GptRecommendationRequest;
import com.capstone.backend.dto.GptRecommendationResponse;
import com.capstone.backend.service.GptRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


    @RestController
    @RequestMapping("/api/gpt")
    @RequiredArgsConstructor
    public class GptRecommendationController {

        private final GptRecommendationService recommendationService;

        @PostMapping("/recommend")
        public ResponseEntity<GptRecommendationResponse> recommend(@RequestBody GptRecommendationRequest request) {
            GptRecommendationResponse result = recommendationService.recommend(request.getMessage());
            return ResponseEntity.ok(result);
        }
    }
