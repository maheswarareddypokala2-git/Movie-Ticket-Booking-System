package com.takehome.moviebooking.repository;
import com.takehome.moviebooking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface BookingRepository extends JpaRepository<Booking, UUID> { Optional<Booking> findByIdempotencyKey(String key); }