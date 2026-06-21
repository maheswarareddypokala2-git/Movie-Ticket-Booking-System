package com.takehome.moviebooking.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shows")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Show {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID screenId;
    private UUID movieId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ShowStatus status;

    public enum ShowStatus { SCHEDULED, CANCELLED, COMPLETED }
}
