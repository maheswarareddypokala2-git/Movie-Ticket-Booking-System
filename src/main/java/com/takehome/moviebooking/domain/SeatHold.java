package com.takehome.moviebooking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "seat_holds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatHold {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID showId;
    private UUID seatId;
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private HoldStatus status;

    private LocalDateTime heldAt;
    private LocalDateTime expiresAt;
    private String redisLockKey;

    public enum HoldStatus { HELD, EXPIRED, CONSUMED }
}
