package com.capstone.backend.service;

import com.capstone.backend.entity.PromptTemplate;
import com.capstone.backend.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private final PromptTemplateRepository repository;

    public PromptTemplate getByType(String type) {
        return repository.findByType(type).orElseThrow(() ->
                new RuntimeException("PromptTemplate not found for type: " + type));
    }

    public PromptTemplate update(Long id, PromptTemplate dto) {
        PromptTemplate existing = repository.findById(id).orElseThrow();
        existing.setType(dto.getType());
        existing.setContent(dto.getContent());
        return repository.save(existing);
    }
}
