package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByPerformanceId(Long performanceId);
    List<Ticket> findByUserId(Long userId);
    boolean existsByPerformanceIdAndSeatNumberAndIdNot(Long performanceId, int seatNumber, Long id);
    boolean existsByPerformanceIdAndSeatNumber(Long performanceId, int seatNumber);
}
