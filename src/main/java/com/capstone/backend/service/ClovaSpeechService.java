package com.capstone.backend.service;

import com.capstone.backend.dto.SSTRangeSplitDTO;
import com.capstone.backend.dto.SSTResponseDTO;
import com.capstone.backend.dto.SegmentDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClovaSpeechService {

    @Value("${clova.secret-key}")
    private String secretKey;

    @Value("${clova.base-url}")
    private String baseUrl;

    public String sendAudioToClova(InputStream audioStream) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .setSocketTimeout(180 * 1000)
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            HttpPost httpPost = new HttpPost(baseUrl + "/recognizer/upload");
            httpPost.setHeader(new BasicHeader("Accept", "application/json"));
            httpPost.setHeader(new BasicHeader("X-CLOVASPEECH-API-KEY", secretKey));

            // InputStream → byte[]
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            audioStream.transferTo(buffer);
            byte[] audioBytes = buffer.toByteArray();

            // Clova 요청 파라미터
            String paramsJson = """
        {
          "language": "ko-KR",
          "completion": "sync",
          "wordAlignment": false,
          "fullText": true,
          "diarization": {
            "enable": false
          }
        }
        """;

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(StandardCharsets.UTF_8);
            builder.addTextBody("params", paramsJson, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("media", audioBytes, ContentType.create("audio/mpeg"), "lecture.mp3");

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
                    return jsonNode.get("text").asText();
                } else {
                    log.error("Clova API Error: {}", responseBody);
                    throw new RuntimeException("Clova API 호출 실패 - 상태 코드: " + statusCode);
                }
            }
        } catch (Exception e) {
            log.error("Clova 호출 중 예외 발생", e);
            throw new RuntimeException("Clova 호출 중 오류 발생", e);
        }
    }
    public SSTResponseDTO sendAudioToClovaWithTimestamps(File audioFile) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(baseUrl + "/recognizer/upload");
            httpPost.setHeader(new BasicHeader("Accept", "application/json"));
            httpPost.setHeader(new BasicHeader("X-CLOVASPEECH-API-KEY", secretKey));

            String fileName = audioFile.getName();
            ContentType contentType = fileName.endsWith(".mp3") ?
                    ContentType.create("audio/mpeg") :
                    ContentType.DEFAULT_BINARY;

            String paramsJson = """
        {
          "language": "ko-KR",
          "completion": "sync",
          "wordAlignment": true,
          "fullText": true,
          "diarization": {
            "enable": false
          }
        }
        """;

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(StandardCharsets.UTF_8);
            builder.addTextBody("params", paramsJson, ContentType.APPLICATION_JSON);
            builder.addBinaryBody("media", audioFile, contentType, fileName);

            httpPost.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode result = mapper.readTree(responseBody);

                    String fullText = result.path("text").asText();
                    List<SegmentDTO> segments = new ArrayList<>();

                    JsonNode segmentsNode = result.path("segments");
                    if (segmentsNode.isArray()) {
                        for (JsonNode seg : segmentsNode) {
                            int start = seg.path("start").asInt();
                            int end = seg.path("end").asInt();
                            String text = seg.path("text").asText();
                            segments.add(new SegmentDTO(start, end, text));
                        }
                    }

                    return new SSTResponseDTO(fullText, segments, true);
                } else {
                    log.error("Clova API Error: {}", responseBody);
                    throw new RuntimeException("Clova API 호출 실패 - 상태 코드: " + statusCode);
                }
            }
        } catch (Exception e) {
            log.error("Clova 호출 중 예외 발생", e);
            throw new RuntimeException("Clova 호출 중 오류 발생", e);
        }
    }

    public SSTRangeSplitDTO splitByTimeRange(SSTResponseDTO sst, int startMillis, int endMillis) {
        List<SegmentDTO> fullSegments = sst.getSegments();
        List<SegmentDTO> rangeSegments = new ArrayList<>();
        List<SegmentDTO> otherSegments = new ArrayList<>();

        for (SegmentDTO segment : fullSegments) {
            if (segment.getEnd() <= startMillis || segment.getStart() >= endMillis) {
                otherSegments.add(segment);
            } else {
                rangeSegments.add(segment);
            }
        }

        return new SSTRangeSplitDTO(fullSegments, rangeSegments, otherSegments);
    }

    public String joinSegmentsText(List<SegmentDTO> segments) {
        return segments.stream()
                .map(SegmentDTO::getText)
                .collect(Collectors.joining(" "));
    }

    public Map<String, String> splitTextByRange(SSTRangeSplitDTO splitDTO) {
        String rangeText = joinSegmentsText(splitDTO.getRangeSegments());
        String otherText = joinSegmentsText(splitDTO.getOtherSegments());

        Map<String, String> result = new HashMap<>();
        result.put("rangeText", rangeText);
        result.put("otherText", otherText);
        return result;
    }

}
