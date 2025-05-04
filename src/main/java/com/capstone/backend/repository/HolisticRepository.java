package com.capstone.backend.repository;

import com.capstone.backend.entity.Holistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolisticRepository extends JpaRepository<Holistic, Long> {
    boolean existsByVideoId(String videoId);
    Holistic findByVideoId(String videoId);
}
