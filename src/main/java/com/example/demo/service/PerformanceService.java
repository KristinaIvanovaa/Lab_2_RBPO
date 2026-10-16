package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.PerformanceRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Performance;
import com.example.demo.model.PerformanceStatus;
import com.example.demo.repository.PerformanceRepository;
import com.example.demo.repository.PlayRepository;
import com.example.demo.repository.TheatreHallRepository;
import com.example.demo.repository.TicketRepository;

@Service
@Transactional
public class PerformanceService {
    private final PerformanceRepository performances;
    private final PlayRepository plays;
    private final TheatreHallRepository halls;
    private final TicketRepository tickets;

    public PerformanceService(PerformanceRepository performances, PlayRepository plays,
                              TheatreHallRepository halls, TicketRepository tickets) {
        this.performances = performances;
        this.plays = plays;
        this.halls = halls;
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public List<Performance> findAll() { return performances.findAll(); }

    @Transactional(readOnly = true)
    public Performance findById(Long id) { return performances.findById(id).orElseThrow(() -> new ResourceNotFoundException("Performance", id)); }

    public Performance create(PerformanceRequest request) {
        require(request.playId() != null && request.hallId() != null
                && request.startsAt() != null && request.basePrice() != null,
                "playId, hallId, startsAt and basePrice are required");
        var play = plays.findById(request.playId())
                .orElseThrow(() -> new ResourceNotFoundException("Play", request.playId()));
        var hall = halls.findById(request.hallId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", request.hallId()));
        return performances.save(new Performance(play, hall, request.startsAt(), request.basePrice(),
                request.status() == null ? PerformanceStatus.SCHEDULED : request.status()));
    }

    public Performance update(Long id, PerformanceRequest request) {
        Performance performance = findById(id);
        if (request.playId() != null) {
            performance.setPlay(plays.findById(request.playId())
                    .orElseThrow(() -> new ResourceNotFoundException("Play", request.playId())));
        }
        if (request.hallId() != null) {
            performance.setHall(halls.findById(request.hallId())
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre hall", request.hallId())));
        }
        if (request.startsAt() != null) performance.setStartsAt(request.startsAt());
        if (request.basePrice() != null) performance.setBasePrice(request.basePrice());
        if (request.status() != null) performance.setStatus(request.status());
        validateExistingSeats(performance);
        return performances.save(performance);
    }

    public void delete(Long id) {
        findById(id);
        tickets.deleteAll(tickets.findByPerformanceId(id));
        performances.deleteById(id);
    }

    private void validateExistingSeats(Performance performance) {
        if (tickets.findByPerformanceId(performance.getId()).stream()
                .anyMatch(ticket -> ticket.getSeatNumber() > performance.getHall().getSeatCount())) {
            throw new IllegalArgumentException("The selected hall has fewer seats than existing tickets");
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
