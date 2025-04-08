package com.capstone.backend.controller;

import com.capstone.backend.entity.Criteria;
import com.capstone.backend.service.CriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/criteria")
public class CriteriaAdminController {

    private final CriteriaService service;
    @GetMapping("/{type}")
    public List<Criteria> getByType(@PathVariable String type) {
        return service.getByType(type);
    }

    @PostMapping
    public Criteria create(@RequestBody Criteria dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public Criteria update(@PathVariable Long id, @RequestBody Criteria dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
