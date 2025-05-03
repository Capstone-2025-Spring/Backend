package com.capstone.backend.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;


@Service
public class AudioService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String flaskBaseUrl = "http://3.39.25.136:5000";

    /**
     * MP3 파일을 분석하고 결과(JSON)를 반환합니다.
     */
    public String analyzeAudio(File mp3File) {
        String url = flaskBaseUrl + "/analyze-audio";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(mp3File));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        Map<String, Object> rawMap = response.getBody();

        if (rawMap == null || !rawMap.containsKey("duration") || !rawMap.containsKey("zcr_mean")) {
            return "오디오 분석 결과가 올바르지 않습니다.";
        }

        try {
            double duration = Double.parseDouble(String.valueOf(rawMap.get("duration")));
            double zcr = Double.parseDouble(String.valueOf(rawMap.get("zcr_mean")));

            return String.format("전체 길이 : %.2f초\n발음의 세기 : %.4f", duration, zcr);
        } catch (Exception e) {
            return "오디오 분석 결과를 파싱하는 데 실패했습니다.";
        }
    }

}

