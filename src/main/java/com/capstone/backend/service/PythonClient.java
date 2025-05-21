package com.capstone.backend.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class PythonClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> sendTextToPython(String text) {
        String url = "http://3.39.25.136:5000/vocab-check";  // EC2 배포 시엔 IP 변경
        //String url = "http://localhost:5000/vocab-check";

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

    public Map<String, Object> analyzeAudioFile(File audioFile) {
        String url = "http://3.39.25.136:5000/analyze-audio";  // Python 서버의 오디오 분석 엔드포인트

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audioFile));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        return response.getBody();
    }
}
