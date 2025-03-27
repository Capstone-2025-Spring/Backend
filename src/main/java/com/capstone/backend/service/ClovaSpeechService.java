package com.capstone.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
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

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ClovaSpeechService {

    @Value("${clova.secret-key}")
    private String secretKey;

    @Value("${clova.base-url}")
    private String baseUrl;

    public String sendAudioToClova(File mp3File) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl + "/recognizer/upload");
            httpPost.setHeader(new BasicHeader("Accept", "application/json"));
            httpPost.setHeader(new BasicHeader("X-CLOVASPEECH-API-KEY", secretKey));

            // 요청 본문 구성
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

            HttpEntity entity = MultipartEntityBuilder.create()
                    .addTextBody("params", paramsJson, ContentType.APPLICATION_JSON)
                    .addBinaryBody("media", mp3File, ContentType.DEFAULT_BINARY, mp3File.getName())
                    .build();

            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("📢 Clova 응답 결과: {}", responseBody);
                return responseBody;
            }
        } catch (Exception e) {
            throw new RuntimeException("Clova 요청 중 오류 발생: " + e.getMessage(), e);
        }
    }
    public String recognizeSpeech(File mp3File) {
        String responseJson = sendAudioToClova(mp3File);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);

            JsonNode fullTextNode = root.path("text");
            if (!fullTextNode.isMissingNode()) {
                return fullTextNode.asText();
            } else {
                throw new RuntimeException("Clova 응답에서 fullText를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Clova 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

}
