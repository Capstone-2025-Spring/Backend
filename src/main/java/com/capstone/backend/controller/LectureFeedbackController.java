package com.capstone.backend.controller;

import com.capstone.backend.dto.LectureEvaluationMP3RequestDTO;
import com.capstone.backend.dto.LectureEvaluationRequestDTO;
import com.capstone.backend.dto.LectureFeedbackResultDTO;
import com.capstone.backend.dto.LectureUploadAudioRespondDTO;
import com.capstone.backend.service.ClovaSpeechService;
import com.capstone.backend.service.GptService;
import com.capstone.backend.service.LectureFeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lecture")
public class LectureFeedbackController {

    private final ClovaSpeechService clovaSpeechService;
    private final GptService gptService;

    @PostMapping("/feedback/text")
    public ResponseEntity<LectureFeedbackResultDTO> getFullEvaluationPipelineByText(@RequestBody LectureEvaluationRequestDTO requestDTO) {
        try {
            String result = gptService.runFullEvaluationPipeline(
                    requestDTO.getLectureText(),
                    requestDTO.getAudioInfo(),
                    requestDTO.getMotionInfo(),
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

    @PostMapping(value = "/feedback/mp3", consumes = "multipart/form-data")
    public ResponseEntity<LectureFeedbackResultDTO> getFullEvaluationPipelineByMP3(
            @RequestParam("file") MultipartFile file,
            @RequestParam("json") String requestJson // LectureEvaluationMP3RequestDTO를 JSON 문자열로 받음
    ) {
        try {
            // 1. 파일 확장자 검사
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
                LectureFeedbackResultDTO errorDto = new LectureFeedbackResultDTO();
                errorDto.setResult("MP3 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(errorDto);
            }

            // 2. 파일 → Clova STT
            String transcript = clovaSpeechService.sendAudioToClova(file.getInputStream());

            // 3. JSON 문자열 → DTO 변환
            ObjectMapper objectMapper = new ObjectMapper();
            LectureEvaluationMP3RequestDTO requestDTO = objectMapper.readValue(requestJson, LectureEvaluationMP3RequestDTO.class);

            // 4. GPT 파이프라인 실행
            String result = gptService.runFullEvaluationPipeline(
                    transcript,
                    requestDTO.getAudioInfo(),
                    requestDTO.getMotionInfo(),
                    "",
                    ""
            );

            // 5. 응답 DTO 생성
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
