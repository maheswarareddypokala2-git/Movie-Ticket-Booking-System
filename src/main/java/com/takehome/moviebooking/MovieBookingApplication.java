package com.takehome.moviebooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point.
 *
 * @EnableScheduling powers the 3-minute cron fallback job (see scheduler package)
 * that catches any seat holds the Redis delay queue or Kafka event path might miss.
 */
@SpringBootApplication
@EnableScheduling
public class MovieBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieBookingApplication.class, args);
    }
}
