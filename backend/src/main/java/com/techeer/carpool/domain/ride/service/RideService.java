package com.techeer.carpool.domain.ride.service;

import com.techeer.carpool.domain.ride.dto.*;
import com.techeer.carpool.domain.ride.entity.Ride;
import com.techeer.carpool.domain.ride.entity.RidePassenger;
import com.techeer.carpool.domain.ride.repository.RidePassengerRepository;
import com.techeer.carpool.domain.ride.repository.RideRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideService {

    private final RideRepository rideRepository;
    private final RidePassengerRepository ridePassengerRepository;

    @Transactional
    public RideResponse createRide(RideCreateRequest request, Long driverId) {
        Ride ride = Ride.builder()
                .postId(request.getPostId())
                .driverId(driverId)
                .build();
        return RideResponse.from(rideRepository.save(ride));
    }

    public RideResponse getRide(Long rideId) {
        return RideResponse.from(findRideById(rideId));
    }

    public List<RideResponse> getMyRidesAsDriver(Long driverId) {
        return rideRepository.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(RideResponse::from)
                .collect(Collectors.toList());
    }

    public List<RideResponse> getMyRidesAsPassenger(Long passengerId) {
        return ridePassengerRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId).stream()
                .map(p -> RideResponse.from(p.getRide()))
                .collect(Collectors.toList());
    }

    @Transactional
    public RideResponse startRide(Long rideId, Long requesterId) {
        Ride ride = findRideById(rideId);
        validateDriver(ride, requesterId);
        ride.start();
        return RideResponse.from(ride);
    }

    @Transactional
    public RideResponse completeRide(Long rideId, Long requesterId) {
        Ride ride = findRideById(rideId);
        validateDriver(ride, requesterId);
        ride.complete();
        return RideResponse.from(ride);
    }

    @Transactional
    public LocationResponse updateLocation(Long rideId, LocationUpdateRequest request, Long requesterId) {
        Ride ride = findRideById(rideId);
        validateDriver(ride, requesterId);
        ride.updateLocation(request.getLatitude(), request.getLongitude());
        return LocationResponse.from(ride);
    }

    public LocationResponse getLocation(Long rideId) {
        return LocationResponse.from(findRideById(rideId));
    }

    public List<PassengerResponse> getPassengers(Long rideId) {
        Ride ride = findRideById(rideId);
        return ride.getPassengers().stream()
                .map(PassengerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PassengerResponse boardPassenger(Long rideId, Long applicationId, Long requesterId) {
        Ride ride = findRideById(rideId);
        validateDriver(ride, requesterId);
        RidePassenger passenger = findPassenger(rideId, applicationId);
        passenger.board();
        return PassengerResponse.from(passenger);
    }

    @Transactional
    public PassengerResponse dropOffPassenger(Long rideId, Long applicationId, Long requesterId) {
        Ride ride = findRideById(rideId);
        validateDriver(ride, requesterId);
        RidePassenger passenger = findPassenger(rideId, applicationId);
        passenger.dropOff();
        return PassengerResponse.from(passenger);
    }

    private Ride findRideById(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("운행을 찾을 수 없습니다. id=" + rideId));
    }

    private RidePassenger findPassenger(Long rideId, Long applicationId) {
        return ridePassengerRepository.findByRideIdAndApplicationId(rideId, applicationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "탑승자를 찾을 수 없습니다. rideId=" + rideId + ", applicationId=" + applicationId));
    }

    private void validateDriver(Ride ride, Long requesterId) {
        if (!ride.getDriverId().equals(requesterId)) {
            throw new IllegalStateException("운행 제어 권한이 없습니다.");
        }
    }
}
