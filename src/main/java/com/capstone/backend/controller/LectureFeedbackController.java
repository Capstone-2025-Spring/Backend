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
    private final PromptTemplateService promptTemplateService;
    private final CriteriaService criteriaService;
    private final AudioService audioService;
    private final MotionService motionService;
    private final ConfigService configService;
    private final MotionCaptionService motionCaptionService;
    private final SSTService sstService;

    @PostMapping("/feedback/text")
    public ResponseEntity<LectureFeedbackResultDTO> getFullEvaluationPipelineByText(@RequestBody LectureEvaluationRequestDTO requestDTO) {
        try {
            String result = gptService.runFullEvaluationPipeline(
                    requestDTO.getLectureText(),
                    requestDTO.getAudioInfo(),
                    requestDTO.getMotionInfo(),
                    "",
                    "",
                    ""
            );

            LectureFeedbackResultDTO responseDto = new LectureFeedbackResultDTO();
            responseDto.setResult(result);

            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            LectureFeedbackResultDTO errorDto = new LectureFeedbackResultDTO();
            errorDto.setResult("에러 발생: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorDto);
        }
    }

    private File convertToTempFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        File tempFile = File.createTempFile("upload_", "_" + (originalFilename != null ? originalFilename : "temp.mp3"));
        multipartFile.transferTo(tempFile);
        tempFile.deleteOnExit(); // JVM 종료 시 자동 삭제
        return tempFile;
    }

    @PostMapping(value = "/feedback/mp3", consumes = "multipart/form-data")
    public ResponseEntity<LectureFeedbackResultDTO> getFullEvaluationPipelineByMP3(
            @RequestParam("file") MultipartFile file,
            @RequestParam("holistic") MultipartFile holistic,
            @RequestParam("config") MultipartFile config //config 파일도 수신
    ) {
        try {
            // 1. 파일 유효성 검사
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
                LectureFeedbackResultDTO errorDto = new LectureFeedbackResultDTO();
                errorDto.setResult("MP3 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(errorDto);
            }
            //Config 파일 Dto 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigRequestDTO configDto = objectMapper.readValue(config.getBytes(), ConfigRequestDTO.class);
            String configInfo = configDto.toSummaryString();

            // Config 저장
            configService.save(configDto);

            // 2. MultipartFile을 File로 저장
            File mp3File = convertToTempFile(file);
            String audioResult = audioService.analyzeAudio(mp3File);

            // 3. Clova STT (InputStream은 미리 복사해서 사용)
            String transcript;
            try (InputStream is = new FileInputStream(mp3File)) {
                transcript = clovaSpeechService.sendAudioToClova(is);
            }

            // SST 저장
            sstService.save(transcript);

            // 4. Motion 분석
            String motionCapture = motionService.getCaptionResult(holistic.getBytes());

            // MotionCaption 저장
            motionCaptionService.save(motionCapture);

            // 5. 평가 기준
            String criteriaCoT = criteriaService.getByType("CoT").stream()
                    .map(Criteria::getContent).filter(c -> c != null && !c.isBlank()).collect(Collectors.joining("\n"));

            String criteriaGEval = criteriaService.getByType("GEval").stream()
                    .map(Criteria::getContent).filter(c -> c != null && !c.isBlank()).collect(Collectors.joining("\n"));

            System.out.println(audioResult);
            System.out.println(motionCapture);

            // 6. GPT 평가 파이프라인 실행
            String result = gptService.runFullEvaluationPipeline(
                    transcript,
                    audioResult,
                    motionCapture,
                    configInfo,
                    criteriaCoT,
                    criteriaGEval
            );


            LectureFeedbackResultDTO responseDto = new LectureFeedbackResultDTO();
            responseDto.setResult(result);
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            e.printStackTrace();
            LectureFeedbackResultDTO errorDto = new LectureFeedbackResultDTO();
            errorDto.setResult("에러 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorDto);
        }
    }


}
