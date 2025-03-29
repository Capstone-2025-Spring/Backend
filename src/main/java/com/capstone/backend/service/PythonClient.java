package com.capstone.backend.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PythonClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> sendTextToPython(String text) {
        String url = "http://localhost:5000/vocab-check";  // EC2 배포 시엔 IP 변경

        // 요청 본문 구성
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // 요청 전송
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        return response.getBody();
    }
}
