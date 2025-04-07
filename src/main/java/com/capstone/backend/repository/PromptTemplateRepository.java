package com.capstone.backend.repository;

import com.capstone.backend.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    Optional<PromptTemplate> findByType(String type);
}

