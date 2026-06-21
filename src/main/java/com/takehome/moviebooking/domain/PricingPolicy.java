package com.takehome.moviebooking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pricing_policies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingPolicy {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Seat.Tier tier;

    private BigDecimal weekdayPrice;
    private BigDecimal weekendPrice;
}
