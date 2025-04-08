package com.capstone.backend.repository;

import com.capstone.backend.entity.Criteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CriteriaRepository extends JpaRepository<Criteria, Long> {
    boolean existsByTypeAndContent(String type, String content);
    List<Criteria> findAllByType(String type);

}
