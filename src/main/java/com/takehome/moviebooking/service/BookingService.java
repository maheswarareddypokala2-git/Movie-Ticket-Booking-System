package com.takehome.moviebooking.service;

import com.takehome.moviebooking.domain.*;
import com.takehome.moviebooking.repository.*;
import com.takehome.moviebooking.redis.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final PricingPolicyRepository pricingRepository;
    private final DiscountCodeRepository discountRepository;
    private final DiscountRuleEngine ruleEngine;

    @Transactional
    public Booking confirmBooking(UUID userId, UUID showId, List<UUID> seatIds, String promoCode, String idempotencyKey) {
        // Idempotency check
        Optional<Booking> existing = bookingRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) return existing.get();

        Show show = showRepository.findById(showId).orElseThrow(() -> new RuntimeException("Show missing"));
        boolean isWeekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(show.getStartTime().getDayOfWeek());

        BigDecimal total = BigDecimal.ZERO;
        List<Seat> verifiedSeats = new ArrayList<>();

        for (UUID seatId : seatIds) {
            // High source-of-truth lock block
            Seat seat = seatRepository.findByIdWithPessimisticLock(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat allocation failed"));
            verifiedSeats.add(seat);

            PricingPolicy policy = pricingRepository.findByTier(seat.getTier())
                    .orElseThrow(() -> new RuntimeException("Pricing configuration missing"));

            BigDecimal basePrice = isWeekend ? policy.getWeekendPrice() : policy.getWeekdayPrice();
            total = total.add(basePrice);
        }

        if (promoCode != null && !promoCode.isEmpty()) {
            DiscountCode discount = discountRepository.findByCode(promoCode).orElseThrow(() -> new RuntimeException("Invalid code"));
            if (ruleEngine.validateCode(discount, verifiedSeats)) {
                if (discount.getDiscountType() == DiscountCode.DiscountType.PERCENTAGE) {
                    BigDecimal reduction = total.multiply(discount.getDiscountValue()).divide(BigDecimal.valueOf(100));
                    total = total.subtract(reduction);
                } else {
                    total = total.subtract(discount.getDiscountValue());
                }
                discount.setUsageCount(discount.getUsageCount() + 1);
                discountRepository.save(discount);
            }
        }

        Booking booking = Booking.builder()
                .userId(userId)
                .showId(showId)
                .status(Booking.BookingStatus.CONFIRMED)
                .totalAmount(total)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        return bookingRepository.save(booking);
    }
}
