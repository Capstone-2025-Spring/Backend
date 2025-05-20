package com.capstone.backend.service;

import com.capstone.backend.dto.MotionCaptionDTO;
import com.capstone.backend.dto.MotionRangeSplitDTO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MotionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String flaskUrl = "http://3.39.25.136:5000/generate-caption";

    //private final String flaskUrl = "http://localhost:5000/generate-caption";

    public MotionRangeSplitDTO splitMotionCaptionByRange(String jsonResponse, int fromSec, int toSec) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("🔥 받은 jsonResponse = " + jsonResponse);
        JsonNode root = mapper.readTree(jsonResponse);
        String raw = root.path("motionInfo").asText();

        List<MotionCaptionDTO> full = parseCompressedMotionCaptions(raw, 0, Integer.MAX_VALUE);
        List<MotionCaptionDTO> range = new ArrayList<>();
        List<MotionCaptionDTO> other = new ArrayList<>();

        for (MotionCaptionDTO dto : full) {
            if (dto.getEndSecond() < fromSec || dto.getStartSecond() > toSec) {
                other.add(dto);
            } else {
                range.add(dto);
            }
        }

        return new MotionRangeSplitDTO(full, range, other);
    }

    public List<MotionCaptionDTO> parseCompressedMotionCaptions(String motionInfo, int fromSec, int toSec) {
        List<MotionCaptionDTO> result = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2}):(\\d{2}):(\\d{2})] (\\d+)");
        Matcher matcher = pattern.matcher(motionInfo);

        while (matcher.find()) {
            int startMin = Integer.parseInt(matcher.group(1));
            int startSec = Integer.parseInt(matcher.group(2));
            int endMin = Integer.parseInt(matcher.group(3));
            int endSec = Integer.parseInt(matcher.group(4));
            String label = matcher.group(5);

            int startTotal = startMin * 60 + startSec;
            int endTotal = endMin * 60 + endSec;

            if (endTotal < fromSec || startTotal > toSec) continue;

            String fromStr = String.format("%02d:%02d", startMin, startSec);
            String toStr = String.format("%02d:%02d", endMin, endSec);

            result.add(new MotionCaptionDTO(fromStr, toStr, startTotal, endTotal, label));
        }

        return result;
    }

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

    public String formatRangeCaptionsCompressed(List<MotionCaptionDTO> rangeCaptions) {
        return rangeCaptions.stream()
                .map(dto -> String.format("[%s]:[%s] %s", dto.getFrom(), dto.getTo(), dto.getLabel()))
                .collect(Collectors.joining(" "));
    }

}
