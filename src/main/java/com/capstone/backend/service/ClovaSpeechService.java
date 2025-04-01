package com.capstone.backend.service;

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

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ClovaSpeechService {

    @Value("${clova.secret-key}")
    private String secretKey;

    @Value("${clova.base-url}")
    private String baseUrl;

    public String sendAudioToClova(File audioFile) {
        // 타임아웃 설정: 3분 (180초)
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

            // 파일 확장자에 따라 ContentType 설정
            String fileName = audioFile.getName();
            ContentType contentType = fileName.endsWith(".mp3") ?
                    ContentType.create("audio/mpeg") :
                    ContentType.DEFAULT_BINARY;

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
            builder.addBinaryBody("media", audioFile, contentType, fileName);

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
}
