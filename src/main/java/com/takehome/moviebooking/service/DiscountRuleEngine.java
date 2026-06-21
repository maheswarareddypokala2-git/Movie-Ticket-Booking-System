package com.takehome.moviebooking.service;

import com.takehome.moviebooking.domain.DiscountCode;
import com.takehome.moviebooking.domain.Seat;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DiscountRuleEngine {

    public boolean validateCode(DiscountCode code, List<Seat> seats) {
        if (!code.isActive()) return false;
        if (code.getValidFrom() != null && LocalDateTime.now().isBefore(code.getValidFrom())) return false;
        if (code.getValidTo() != null && LocalDateTime.now().isAfter(code.getValidTo())) return false;
        if (code.getUsageLimit() != null && code.getUsageCount() >= code.getUsageLimit()) return false;
        if (code.getMinSeats() != null && seats.size() < code.getMinSeats()) return false;
        if (code.getApplicableTier() != null) {
            boolean matchesTier = seats.stream().anyMatch(s -> s.getTier() == code.getApplicableTier());
            if (!matchesTier) return false;
        }
        return true;
    }
}
