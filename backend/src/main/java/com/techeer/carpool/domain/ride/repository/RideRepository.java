package com.techeer.carpool.domain.ride.repository;

import com.techeer.carpool.domain.ride.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findAllByDriverIdOrderByCreatedAtDesc(Long driverId);
}