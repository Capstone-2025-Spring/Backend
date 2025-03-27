package com.capstone.backend.controller;

import com.capstone.backend.service.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gpt")
public class GptController {

    private final GptService gptService;

    @PostMapping("/feedback")
    public ResponseEntity<String> getFeedback(@RequestBody String text) {
        String result = gptService.getFeedbackFromGpt(text);
        return ResponseEntity.ok(result);
    }
}
