package com.takehome.moviebooking.repository;
import com.takehome.moviebooking.domain.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface ShowRepository extends JpaRepository<Show, UUID> {}