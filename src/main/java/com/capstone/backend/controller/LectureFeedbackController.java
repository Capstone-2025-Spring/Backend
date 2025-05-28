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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lecture")
public class LectureFeedbackController {

    private final ClovaSpeechService clovaSpeechService;
    private final GptService gptService;
    private final GPTEventService gptEventService;
    private final VocabService vocabService;
    private final CriteriaService criteriaService;
    private final AudioService audioService;
    private final MotionService motionService;
    private final ConfigService configService;
    private final MotionCaptionService motionCaptionService;
    private final SSTService sstService;
    private final GPTUserCriteriaService gptUserCriteriaService;
    private final GPTMotionCaptionService gptMotionCaptionService;

    private File convertToTempFile(MultipartFile multipartFile) throws IOException {
        String ext = multipartFile.getOriginalFilename() != null && multipartFile.getOriginalFilename().contains(".")
                ? multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."))
                : ".tmp";
        File tempFile = File.createTempFile("upload_", "_" + UUID.randomUUID() + ext);
        multipartFile.transferTo(tempFile);
        tempFile.deleteOnExit();
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
            System.out.println("------모션 캡션 정보-------");
            System.out.println(motionCapture);
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
            CompletableFuture<EvaluationResultDTO> generalEvalFuture = CompletableFuture.supplyAsync(() -> {
                long subStart = System.currentTimeMillis();
                EvaluationResultDTO dto = gptService.runFullEvaluationPipeline(
                        transcript,
                        audioResult,
                        motionCapture,
                        configInfo,
                        criteriaCoT,
                        criteriaGEval
                );
                dto.setVocabDifficulty(difficulty);
                dto.setBlockedWords(blockedWords);
                dto.setEventReason("");
                dto.setEventScore("");
                long subEnd = System.currentTimeMillis();
                System.out.println("🟥 일반 평가 GPT 소요 시간: " + (subEnd - subStart) + "ms");
                return dto;
            });

            CompletableFuture<EvaluationResultDTO> userEvalFuture = CompletableFuture.supplyAsync(() -> {
                long subStart = System.currentTimeMillis();
                EvaluationResultDTO eventResult = gptUserCriteriaService.getCustomEvaluation(configDto, transcript, motionCapture);
                long subEnd = System.currentTimeMillis();
                System.out.println("🟩 유저 Criteria 평가 GPT 소요 시간: " + (subEnd - subStart) + "ms");
                return eventResult;
            });

            CompletableFuture<MotionEvaluationDTO> motionEvalFuture = CompletableFuture.supplyAsync(() -> {
                long subStart = System.currentTimeMillis();
                MotionEvaluationDTO motionEval = gptMotionCaptionService.getMotionCaptions(motionCapture);
                long subEnd = System.currentTimeMillis();
                System.out.println("🟦 모션 평가 GPT 소요 시간: " + (subEnd - subStart) + "ms");
                System.out.println("모션 평가 DTO\n" + motionEval);
                return motionEval;
            });

            long gptEnd = System.currentTimeMillis();
            System.out.println("🟥 GPT 평가 파이프라인 소요 시간: " + (gptEnd - gptStart) + "ms");

            long totalEnd = System.currentTimeMillis();
            System.out.println("✅ 전체 처리 소요 시간: " + (totalEnd - totalStart) + "ms");


            EvaluationResultDTO resultDto = generalEvalFuture.get();
            EvaluationResultDTO userCriteriaDto = userEvalFuture.get();
            MotionEvaluationDTO motionEvaluationDTO = motionEvalFuture.get();

            List<EvaluationItemDTO> mergedList = new ArrayList<>();

            if (resultDto.getCriteriaScores() != null) {
                mergedList.addAll(resultDto.getCriteriaScores());
            }
            if (userCriteriaDto.getCriteriaScores() != null) {
                mergedList.addAll(userCriteriaDto.getCriteriaScores());
            }

            resultDto.setCriteriaScores(mergedList);
            resultDto.setMotionCaptions(motionEvaluationDTO.getResults());

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
            @RequestParam("config") MultipartFile config,
            @RequestParam("eventInfo") MultipartFile eventInfoFile
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

            // 2.5 EventInfo 파싱
            long eventInfoStart = System.currentTimeMillis();
            ObjectMapper twoObjectMapper = new ObjectMapper();
            EventInfoDTO eventInfoDto = twoObjectMapper.readValue(eventInfoFile.getBytes(), EventInfoDTO.class);
            String eventDescription = eventInfoDto.getDescription(); // 기존 eventInfo 역할
            int eventStartMs = eventInfoDto.getStart_ms();
            int eventEndMs = eventInfoDto.getEnd_ms();
            long eventInfoEnd = System.currentTimeMillis();
            System.out.println("🟦 EventInfo 파싱 소요 시간: " + (eventInfoEnd - eventInfoStart) + "ms");

            // 3. MP3 분석
            long audioStart = System.currentTimeMillis();
            File mp3File = convertToTempFile(file);
            String audioResult = audioService.analyzeAudio(mp3File);
            long audioEnd = System.currentTimeMillis();
            System.out.println("🟧 MP3 분석 소요 시간: " + (audioEnd - audioStart) + "ms");

            // 4. SST
            long sstStart = System.currentTimeMillis();
            SSTResponseDTO sst = clovaSpeechService.sendAudioToClovaWithTimestamps(mp3File);
            SSTRangeSplitDTO split = clovaSpeechService.splitByTimeRange(sst, eventStartMs, eventEndMs);
            Map<String, String> textMap = clovaSpeechService.splitTextByRange(split);
            String textInRange = textMap.get("rangeText");
            String textOutOfRange = textMap.get("otherText");
            long sstEnd = System.currentTimeMillis();
            System.out.println("🟨 SST 처리 소요 시간: " + (sstEnd - sstStart) + "ms");

            // 4.5 어휘 분석
            long vocabStart = System.currentTimeMillis();
            Map<String, Object> vocabAnalysis = vocabService.analyzeVocabularyDetail(sst.getFullText());
            String difficulty = String.valueOf(vocabAnalysis.getOrDefault("difficulty_level", "분석불가"));
            List<String> blockedWords = (List<String>) vocabAnalysis.getOrDefault("blocked_words", List.of());
            long vocabEnd = System.currentTimeMillis();
            System.out.println(blockedWords);
            System.out.println("📘 어휘 분석 소요 시간: " + (vocabEnd - vocabStart) + "ms");

            // 5. 모션 캡션
            long motionStart = System.currentTimeMillis();
            String motionJsonResponse = motionService.getCaptionResult(holistic.getBytes());
            MotionRangeSplitDTO motionSplit = motionService.splitMotionCaptionByRange(motionJsonResponse, eventStartMs/1000, eventEndMs/1000);
            String rangeMotionCaption = motionService.formatRangeCaptionsCompressed(motionSplit.getRangeCaptions());
            motionCaptionService.save(motionJsonResponse);
            long motionEnd = System.currentTimeMillis();
            System.out.println("🟩 모션 캡션 처리 소요 시간: " + (motionEnd - motionStart) + "ms");

            // 6. 평가 기준 로딩
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

            // 7. GPT 병렬 평가 실행
            long gptStart = System.currentTimeMillis();
            CompletableFuture<EvaluationResultDTO> generalEvalFuture = CompletableFuture.supplyAsync(() -> {
                long subStart = System.currentTimeMillis();
                EvaluationResultDTO dto = gptService.runFullEvaluationPipeline(
                        textOutOfRange,
                        audioResult,
                        motionJsonResponse,
                        configInfo,
                        criteriaCoT,
                        criteriaGEval
                );
                dto.setVocabDifficulty(difficulty);
                dto.setBlockedWords(blockedWords);
                long subEnd = System.currentTimeMillis();
                System.out.println("🟥 일반 평가 GPT 소요 시간: " + (subEnd - subStart) + "ms");
                return dto;
            });

            CompletableFuture<EvaluationResultDTO> eventEvalFuture = CompletableFuture.supplyAsync(() -> {
                long subStart = System.currentTimeMillis();
                EvaluationResultDTO eventResult = gptEventService.getEventEvaluation(eventDescription, textInRange, rangeMotionCaption, configInfo);
                long subEnd = System.currentTimeMillis();
                System.out.println("🟩 이벤트 평가 GPT 소요 시간: " + (subEnd - subStart) + "ms");
                return eventResult;
            });

            CompletableFuture<EvaluationResultDTO> userEvalFuture = CompletableFuture.supplyAsync(() -> {
                long subStart = System.currentTimeMillis();
                EvaluationResultDTO eventResult = gptUserCriteriaService.getCustomEvaluation(configDto, textOutOfRange, motionJsonResponse);
                long subEnd = System.currentTimeMillis();
                System.out.println("🟩 유저 Criteria 평가 GPT 소요 시간: " + (subEnd - subStart) + "ms");
                return eventResult;
            });

            CompletableFuture<MotionEvaluationDTO> motionEvalFuture = CompletableFuture.supplyAsync(() -> {
                long subStart = System.currentTimeMillis();
                MotionEvaluationDTO motionEval = gptMotionCaptionService.getMotionCaptions(motionJsonResponse);
                long subEnd = System.currentTimeMillis();
                System.out.println("🟦 모션 평가 GPT 소요 시간: " + (subEnd - subStart) + "ms");
                System.out.println("모션 평가 DTO\n" + motionEval);
                return motionEval;
            });

            EvaluationResultDTO resultDto = generalEvalFuture.get();
            EvaluationResultDTO eventDto = eventEvalFuture.get();
            EvaluationResultDTO userCriteriaDto = userEvalFuture.get();
            MotionEvaluationDTO motionEvaluationDTO = motionEvalFuture.get();
            resultDto.setEventReason(eventDto.getEventReason());
            resultDto.setEventScore(eventDto.getEventScore());

            List<EvaluationItemDTO> mergedList = new ArrayList<>();

            if (resultDto.getCriteriaScores() != null) {
                mergedList.addAll(resultDto.getCriteriaScores());
            }
            if (userCriteriaDto.getCriteriaScores() != null) {
                mergedList.addAll(userCriteriaDto.getCriteriaScores());
            }

            resultDto.setCriteriaScores(mergedList);
            resultDto.setMotionCaptions(motionEvaluationDTO.getResults());

            long gptEnd = System.currentTimeMillis();
            System.out.println("🟥 GPT 평가 병렬 실행 소요 시간: " + (gptEnd - gptStart) + "ms");

            long totalEnd = System.currentTimeMillis();
            System.out.println("✅ 전체 처리 소요 시간: " + (totalEnd - totalStart) + "ms");

            System.out.println(resultDto);

            return ResponseEntity.ok(resultDto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }


    @PostMapping(value = "/feedback/criteria", consumes = "multipart/form-data")
    public ResponseEntity<String> getFullEvaluationPipelineByCriteria(
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

            // 4. SST
            long sstStart = System.currentTimeMillis();
            SSTResponseDTO sst = clovaSpeechService.sendAudioToClovaWithTimestamps(mp3File);
            SSTRangeSplitDTO split = clovaSpeechService.splitByTimeRange(sst, 120_000, 150_000);
            Map<String, String> textMap = clovaSpeechService.splitTextByRange(split);
            String textInRange = textMap.get("rangeText");
            String textOutOfRange = textMap.get("otherText");
            long sstEnd = System.currentTimeMillis();
            System.out.println("🟨 SST 처리 소요 시간: " + (sstEnd - sstStart) + "ms");

            // 4.5 어휘 분석
            long vocabStart = System.currentTimeMillis();
            Map<String, Object> vocabAnalysis = vocabService.analyzeVocabularyDetail(sst.getFullText());
            String difficulty = String.valueOf(vocabAnalysis.getOrDefault("difficulty_level", "분석불가"));
            List<String> blockedWords = (List<String>) vocabAnalysis.getOrDefault("blocked_words", List.of());
            long vocabEnd = System.currentTimeMillis();
            System.out.println(blockedWords);
            System.out.println("📘 어휘 분석 소요 시간: " + (vocabEnd - vocabStart) + "ms");

            // 5. 모션 캡션
            long motionStart = System.currentTimeMillis();
            String motionJsonResponse = motionService.getCaptionResult(holistic.getBytes());
            MotionRangeSplitDTO motionSplit = motionService.splitMotionCaptionByRange(motionJsonResponse, 120, 150);
            String rangeMotionCaption = motionService.formatRangeCaptionsCompressed(motionSplit.getRangeCaptions());
            motionCaptionService.save(motionJsonResponse);
            long motionEnd = System.currentTimeMillis();
            System.out.println("🟩 모션 캡션 처리 소요 시간: " + (motionEnd - motionStart) + "ms");

            EvaluationResultDTO resultDto = gptUserCriteriaService.getCustomEvaluation(
                    configDto,
                    textOutOfRange,
                    motionJsonResponse
            );
            System.out.println(resultDto);
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }
}
