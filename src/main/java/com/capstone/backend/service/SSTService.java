package com.capstone.backend.service;

import com.capstone.backend.entity.SST;
import com.capstone.backend.repository.SSTRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SSTService {

    private final SSTRepository sstRepository;

    /**
     *
     */
    public SST save(String content) {
        SST sst = SST.builder()
                .content(content)
                .build();
        return sstRepository.save(sst);
    }

    /**
     * 가장 최신 SST 조회
     */
    public Optional<SST> findLatest() {
        return sstRepository.findById(1L);
    }

    /**
     * 단순 content만 반환
     */
    public Optional<SST> getLatestSST() {
        return findLatest();
    }


    /**
     * 전체 SST 리스트 조회
     */
    public List<SST> findAll() {
        return sstRepository.findAll();
    }

    /**
     * ID로 SST 조회
     */
    public Optional<SST> findById(Long id) {
        return sstRepository.findById(id);
    }
}
