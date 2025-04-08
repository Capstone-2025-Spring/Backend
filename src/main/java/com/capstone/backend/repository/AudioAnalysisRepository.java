package com.capstone.backend.repository;

import com.capstone.backend.entity.AudioAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioAnalysisRepository extends JpaRepository<AudioAnalysis, Long> {
}
