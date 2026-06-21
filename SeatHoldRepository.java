package com.takehome.moviebooking.repository;
import com.takehome.moviebooking.domain.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
public interface SeatHoldRepository extends JpaRepository<SeatHold, UUID> { List<SeatHold> findByStatusAndExpiresAtBefore(SeatHold.HoldStatus status, LocalDateTime dateTime); }