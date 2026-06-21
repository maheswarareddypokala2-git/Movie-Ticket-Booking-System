package com.takehome.moviebooking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private UUID showId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private BigDecimal totalAmount;

    @Column(unique = true)
    private String idempotencyKey;

    private LocalDateTime createdAt;

    public enum BookingStatus { PENDING_PAYMENT, CONFIRMED, FAILED, CANCELLED }
}
