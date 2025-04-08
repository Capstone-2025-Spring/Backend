package com.capstone.backend.service;

import com.capstone.backend.entity.Criteria;
import com.capstone.backend.repository.CriteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CriteriaService {

    private final CriteriaRepository repository;

    public List<Criteria> getByType(String type) {
        return repository.findAllByType(type);
    }

    public Criteria create(Criteria dto) {
        return repository.save(dto);
    }

    public Criteria update(Long id, Criteria dto) {
        Criteria existing = repository.findById(id).orElseThrow();
        existing.setType(dto.getType());
        existing.setContent(dto.getContent());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
