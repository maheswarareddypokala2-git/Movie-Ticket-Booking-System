package com.takehome.moviebooking.controller;

import com.takehome.moviebooking.domain.Booking;
import com.takehome.moviebooking.service.BookingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> bookSeats(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody BookingRequest request) {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Booking confirmed = bookingService.confirmBooking(
                UUID.fromString(userIdStr), request.getShowId(), request.getSeatIds(), request.getPromoCode(), idempotencyKey);
        return ResponseEntity.ok(confirmed);
    }

    @Data
    public static class BookingRequest {
        private UUID showId;
        private List<UUID> seatIds;
        private String promoCode;
    }
}
