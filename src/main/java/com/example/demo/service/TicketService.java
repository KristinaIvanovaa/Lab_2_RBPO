package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dto.TicketRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Ticket;
import com.example.demo.model.TicketStatus;
import com.example.demo.repository.PerformanceRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.UserRepository;

@Service
@Transactional
public class TicketService {
    private final TicketRepository tickets;
    private final PerformanceRepository performances;
    private final UserRepository users;

    public TicketService(TicketRepository tickets, PerformanceRepository performances, UserRepository users) {
        this.tickets = tickets;
        this.performances = performances;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAll() { return tickets.findAll(); }

    @Transactional(readOnly = true)
    public Ticket findById(Long id) { return tickets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ticket", id)); }

    public Ticket create(TicketRequest request) {
        require(request.performanceId() != null && request.seatNumber() != null && request.userId() != null,
                "performanceId, seatNumber and userId are required");
        var performance = performances.findById(request.performanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Performance", request.performanceId()));
        var user = users.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        validateSeat(performance.getHall().getSeatCount(), request.seatNumber());
        if (tickets.existsByPerformanceIdAndSeatNumber(performance.getId(), request.seatNumber())) {
            throw new ConflictException("The seat is already reserved or sold for this performance");
        }
        return tickets.save(new Ticket(performance, request.seatNumber(), user,
                request.status() == null ? TicketStatus.RESERVED : request.status()));
    }

    public Ticket update(Long id, TicketRequest request) {
        Ticket ticket = findById(id);
        if (request.performanceId() != null) {
            ticket.setPerformance(performances.findById(request.performanceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Performance", request.performanceId())));
        }
        if (request.seatNumber() != null) ticket.setSeatNumber(request.seatNumber());
        if (request.userId() != null) {
            ticket.setUser(users.findById(request.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.userId())));
        }
        if (request.status() != null) ticket.setStatus(request.status());
        validateSeat(ticket.getPerformance().getHall().getSeatCount(), ticket.getSeatNumber());
        if (tickets.existsByPerformanceIdAndSeatNumberAndIdNot(
                ticket.getPerformance().getId(), ticket.getSeatNumber(), id)) {
            throw new ConflictException("The seat is already reserved or sold for this performance");
        }
        return tickets.save(ticket);
    }

    public void delete(Long id) {
        findById(id);
        tickets.deleteById(id);
    }

    private void validateSeat(int seatCount, int seatNumber) {
        if (seatNumber < 1 || seatNumber > seatCount) {
            throw new IllegalArgumentException("seatNumber must be between 1 and " + seatCount);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
