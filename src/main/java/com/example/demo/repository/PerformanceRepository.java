package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Performance;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    List<Performance> findByPlayId(Long playId);
    List<Performance> findByHallId(Long hallId);
}
