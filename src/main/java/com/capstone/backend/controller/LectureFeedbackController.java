package com.capstone.backend.controller;
import com.capstone.backend.dto.*;
import com.capstone.backend.entity.Criteria;
import com.capstone.backend.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lecture")
public class LectureFeedbackController {

    private final ClovaSpeechService clovaSpeechService;
    private final GptService gptService;
    private final VocabService vocabService;
    private final CriteriaService criteriaService;
    private final AudioService audioService;
    private final MotionService motionService;
    private final ConfigService configService;
    private final MotionCaptionService motionCaptionService;
    private final SSTService sstService;

    private File convertToTempFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        File tempFile = File.createTempFile("upload_", "_" + (originalFilename != null ? originalFilename : "temp.mp3"));
        multipartFile.transferTo(tempFile);
        tempFile.deleteOnExit(); // JVM 종료 시 자동 삭제
        return tempFile;
    }

    @PostMapping(value = "/feedback/mp3", consumes = "multipart/form-data")
    public ResponseEntity<?> getFullEvaluationPipelineByMP3(
            @RequestParam("file") MultipartFile file,
            @RequestParam("holistic") MultipartFile holistic,
            @RequestParam("config") MultipartFile config
    ) {
        try {
            long totalStart = System.currentTimeMillis();

            // 1. 파일 유효성 검사
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
                return ResponseEntity.badRequest().body("MP3 파일만 업로드 가능합니다.");
            }

            // 2. Config 파싱
            long configStart = System.currentTimeMillis();
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigRequestDTO configDto = objectMapper.readValue(config.getBytes(), ConfigRequestDTO.class);
            String configInfo = configDto.toSummaryString();
            configService.save(configDto);
            long configEnd = System.currentTimeMillis();
            System.out.println("🟦 Config 파싱 소요 시간: " + (configEnd - configStart) + "ms");

            // 3. MP3 분석
            long audioStart = System.currentTimeMillis();
            File mp3File = convertToTempFile(file);
            String audioResult = audioService.analyzeAudio(mp3File);
            long audioEnd = System.currentTimeMillis();
            System.out.println("🟧 MP3 분석 소요 시간: " + (audioEnd - audioStart) + "ms");

            // 4. STT 처리
            long sttStart = System.currentTimeMillis();
            String transcript;
            try (InputStream is = new FileInputStream(mp3File)) {
                transcript = clovaSpeechService.sendAudioToClova(is);
            }
            sstService.save(transcript);
            long sttEnd = System.currentTimeMillis();
            System.out.println("🟨 STT 처리 소요 시간: " + (sttEnd - sttStart) + "ms");

            // 4.5 어휘 분석 (상세 정보용)
            long vocabStart = System.currentTimeMillis();
            Map<String, Object> vocabAnalysis = vocabService.analyzeVocabularyDetail(transcript);
            long vocabEnd = System.currentTimeMillis();
            System.out.println("📘 어휘 분석 소요 시간: " + (vocabEnd - vocabStart) + "ms");

            // Optional: 로그 출력용 요약
            String difficulty = String.valueOf(vocabAnalysis.getOrDefault("difficulty_level", "분석불가"));
            List<String> blockedWords = (List<String>) vocabAnalysis.getOrDefault("blocked_words", List.of());
            System.out.println("📘 어휘 난이도: " + difficulty);
            System.out.println("📘 금지 어휘: " + blockedWords);


            // 5. 모션 처리
            long motionStart = System.currentTimeMillis();
            String motionCapture = motionService.getCaptionResult(holistic.getBytes());
            motionCaptionService.save(motionCapture);
            long motionEnd = System.currentTimeMillis();
            System.out.println("🟩 모션 처리 소요 시간: " + (motionEnd - motionStart) + "ms");

            // 6. 평가 기준 불러오기
            long criteriaStart = System.currentTimeMillis();
            String criteriaCoT = criteriaService.getByType("CoT").stream()
                    .map(Criteria::getContent)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.joining("\n"));

            String criteriaGEval = criteriaService.getByType("GEval").stream()
                    .map(Criteria::getContent)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.joining("\n"));
            long criteriaEnd = System.currentTimeMillis();
            System.out.println("🟪 평가 기준 로딩 소요 시간: " + (criteriaEnd - criteriaStart) + "ms");

            // 7. GPT 평가 실행
            long gptStart = System.currentTimeMillis();
            EvaluationResultDTO resultDto = gptService.runFullEvaluationPipeline(
                    transcript,
                    audioResult,
                    motionCapture,
                    configInfo,
                    criteriaCoT,
                    criteriaGEval
            );

            resultDto.setVocabDifficulty(difficulty);
            resultDto.setBlockedWords(blockedWords);

            long gptEnd = System.currentTimeMillis();
            System.out.println("🟥 GPT 평가 파이프라인 소요 시간: " + (gptEnd - gptStart) + "ms");

            long totalEnd = System.currentTimeMillis();
            System.out.println("✅ 전체 처리 소요 시간: " + (totalEnd - totalStart) + "ms");

            return ResponseEntity.ok(resultDto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }

    @PostMapping(value = "/feedback/event", consumes = "multipart/form-data")
    public ResponseEntity<?> getFullEvaluationPipelineByEvent(
            @RequestParam("file") MultipartFile file,
            @RequestParam("holistic") MultipartFile holistic,
            @RequestParam("config") MultipartFile config
    ) {
        try {
            long totalStart = System.currentTimeMillis();

            // 1. 파일 유효성 검사
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
                return ResponseEntity.badRequest().body("MP3 파일만 업로드 가능합니다.");
            }

            // 2. Config 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigRequestDTO configDto = objectMapper.readValue(config.getBytes(), ConfigRequestDTO.class);
            String configInfo = configDto.toSummaryString();
            configService.save(configDto);

            // 3. MP3 분석
            File mp3File = convertToTempFile(file);
            String audioResult = audioService.analyzeAudio(mp3File);

            // 4. SST (Timestamp 포함 버전)
            SSTResponseDTO sst = clovaSpeechService.sendAudioToClovaWithTimestamps(mp3File);
            SSTRangeSplitDTO split = clovaSpeechService.splitByTimeRange(sst, 120_000, 150_000);

            Map<String, String> textMap = clovaSpeechService.splitTextByRange(split);
            String textInRange = textMap.get("rangeText"); // 이벤트 범위 내 텍스트
            String textOutOfRange = textMap.get("otherText"); // 이벤트 범위 밖 텍스트

            System.out.println("구간 내 텍스트:\n\n\n\n" + textInRange +"\n\n\n\n");

            // 4.5 어휘 분석
            Map<String, Object> vocabAnalysis = vocabService.analyzeVocabularyDetail(sst.getFullText());
            String difficulty = String.valueOf(vocabAnalysis.getOrDefault("difficulty_level", "분석불가"));
            List<String> blockedWords = (List<String>) vocabAnalysis.getOrDefault("blocked_words", List.of());

            // 5. 모션 캡션 (timestamp 버전)
            String motionJsonResponse = motionService.getCaptionResult(holistic.getBytes());
            MotionRangeSplitDTO motionSplit = motionService.splitMotionCaptionByRange(motionJsonResponse, 120, 150);
            String rangeMotionCaption = motionService.formatRangeCaptionsCompressed(motionSplit.getRangeCaptions());
            motionCaptionService.save(motionJsonResponse);

            // 6. 평가 기준
            String criteriaCoT = criteriaService.getByType("CoT").stream()
                    .map(Criteria::getContent)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.joining("\n"));
            String criteriaGEval = criteriaService.getByType("GEval").stream()
                    .map(Criteria::getContent)
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.joining("\n"));

            // 7. GPT 평가 실행
            EvaluationResultDTO resultDto = gptService.runFullEvaluationPipeline(
                    sst.getFullText(),
                    audioResult,
                    motionJsonResponse,
                    configInfo,
                    criteriaCoT,
                    criteriaGEval
            );
            resultDto.setVocabDifficulty(difficulty);
            resultDto.setBlockedWords(blockedWords);

            long totalEnd = System.currentTimeMillis();
            System.out.println("✅ 전체 처리 소요 시간: " + (totalEnd - totalStart) + "ms");

            return ResponseEntity.ok(resultDto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }



}
