package com.takehome.moviebooking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "seats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID screenId;
    private String seatCode;
    @Enumerated(EnumType.STRING)
    private Tier tier;
    private boolean isActive = true;

    public enum Tier { REGULAR, PREMIUM }
}