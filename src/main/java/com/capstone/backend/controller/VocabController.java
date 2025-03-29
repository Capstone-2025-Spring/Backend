package com.capstone.backend.controller;
import com.capstone.backend.service.PythonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/vocab")
public class VocabController {

    private final PythonClient pythonClient;

    public VocabController(PythonClient pythonClient) {
        this.pythonClient = pythonClient;
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkVocab(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text 필드가 필요합니다."));
        }

        Map<String, Object> result = pythonClient.sendTextToPython(text);
        return ResponseEntity.ok(result);
    }
}
