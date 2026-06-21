package com.takehome.moviebooking.repository;

import com.takehome.moviebooking.domain.PricingPolicy;
import com.takehome.moviebooking.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PricingPolicyRepository extends JpaRepository<PricingPolicy, UUID> {
    Optional<PricingPolicy> findByTier(Seat.Tier tier);
}
