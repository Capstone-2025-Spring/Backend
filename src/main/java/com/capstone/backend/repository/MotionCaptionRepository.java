package com.capstone.backend.repository;

import com.capstone.backend.entity.Holistic;
import com.capstone.backend.entity.MotionCaption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotionCaptionRepository extends JpaRepository<MotionCaption, Long> {
}
