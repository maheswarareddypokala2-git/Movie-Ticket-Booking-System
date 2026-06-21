package com.takehome.moviebooking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "discount_codes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiscountCode {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private BigDecimal discountValue;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer minSeats;

    @Enumerated(EnumType.STRING)
    private Seat.Tier applicableTier;

    private Integer usageLimit;
    private int usageCount = 0;
    private boolean isActive = true;

    public enum DiscountType { PERCENTAGE, FLAT }
}
