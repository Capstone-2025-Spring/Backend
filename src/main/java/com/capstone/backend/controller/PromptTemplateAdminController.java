package com.capstone.backend.controller;

import com.capstone.backend.entity.PromptTemplate;
import com.capstone.backend.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/prompt")
public class PromptTemplateAdminController {
    private final PromptTemplateService service;

    @GetMapping("/{type}")
    public PromptTemplate getByType(@PathVariable String type) {
        return service.getByType(type);
    }

    @PutMapping("/{id}")
    public PromptTemplate update(@PathVariable Long id, @RequestBody PromptTemplate dto) {
        return service.update(id, dto);
    }
}
