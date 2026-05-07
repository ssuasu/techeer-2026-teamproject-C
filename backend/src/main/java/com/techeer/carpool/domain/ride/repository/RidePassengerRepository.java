package com.techeer.carpool.domain.ride.repository;

import com.techeer.carpool.domain.ride.entity.RidePassenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RidePassengerRepository extends JpaRepository<RidePassenger, Long> {

    Optional<RidePassenger> findByRideIdAndApplicationId(Long rideId, Long applicationId);

    List<RidePassenger> findAllByPassengerIdOrderByCreatedAtDesc(Long passengerId);
}