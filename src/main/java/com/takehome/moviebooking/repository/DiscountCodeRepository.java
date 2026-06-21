package com.takehome.moviebooking.repository;

import com.takehome.moviebooking.domain.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, UUID> {
    Optional<DiscountCode> findByCode(String code);
}
