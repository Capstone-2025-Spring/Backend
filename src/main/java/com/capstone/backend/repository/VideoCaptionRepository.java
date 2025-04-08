package com.capstone.backend.repository;

import com.capstone.backend.entity.VideoCaption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoCaptionRepository extends JpaRepository<VideoCaption, Long> {
}
