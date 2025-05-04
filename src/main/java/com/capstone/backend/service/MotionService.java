package com.capstone.backend.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class MotionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String flaskUrl = "http://3.39.25.136:5000/generate-caption";

    //private final String flaskUrl = "http://localhost:5000/generate-caption";

    public String getCaptionResult(byte[] jsonFileBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(jsonFileBytes) {
            @Override
            public String getFilename() {
                return "input.json";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();  // JSON String 그대로 반환
            } else {
                return "{\"error\": \"Flask 서버에서 유효한 응답을 받지 못했습니다.\"}";
            }

        } catch (Exception e) {
            return "{\"error\": \"Flask 서버 요청 중 오류 발생: " + e.getMessage() + "\"}";
        }
    }
}
