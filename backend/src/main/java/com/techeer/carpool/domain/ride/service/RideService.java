package com.techeer.carpool.domain.ride.service;

import com.techeer.carpool.domain.application.entity.Application;
import com.techeer.carpool.domain.application.entity.ApplicationStatus;
import com.techeer.carpool.domain.application.repository.ApplicationRepository;
import com.techeer.carpool.domain.driver.repository.DriverRepository;
import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.entity.PostStatus;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.domain.ride.dto.*;
import com.techeer.carpool.domain.ride.entity.Ride;
import com.techeer.carpool.domain.ride.entity.RidePassenger;
import com.techeer.carpool.domain.ride.entity.RideStatus;
import com.techeer.carpool.domain.ride.repository.RidePassengerRepository;
import com.techeer.carpool.domain.ride.repository.RideRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
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
    private final PostRepository postRepository;
    private final DriverRepository driverRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public RideResponse createRide(RideCreateRequest request, Long driverId) {
        driverRepository.findByMemberIdAndDeletedFalse(driverId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.DRIVER_NOT_FOUND));

        Post post = postRepository.findByIdAndDeletedFalse(request.getPostId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));
        if (!post.getMemberId().equals(driverId)) {
            throw new CarpoolException(ErrorCode.RIDE_FORBIDDEN);
        }
        if (post.getStatus() != PostStatus.OPEN) {
            throw new CarpoolException(ErrorCode.RIDE_INVALID_STATUS);
        }
        Ride ride = rideRepository.save(Ride.builder()
                .postId(request.getPostId())
                .driverId(driverId)
                .build());

        List<Application> accepted = applicationRepository.findByPostIdAndStatus(
                request.getPostId(), ApplicationStatus.ACCEPTED);
        List<RidePassenger> passengers = accepted.stream()
                .map(app -> RidePassenger.builder()
                        .ride(ride)
                        .applicationId(app.getId())
                        .passengerId(app.getApplicantId())
                        .build())
                .collect(Collectors.toList());
        ridePassengerRepository.saveAll(passengers);

        return RideResponse.from(ride);
    }

    public RideResponse getRide(Long rideId) {
        return RideResponse.from(findRideById(rideId));
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
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new CarpoolException(ErrorCode.RIDE_INVALID_STATUS);
        }
        RidePassenger passenger = findPassenger(rideId, applicationId);
        passenger.board();
        return PassengerResponse.from(passenger);
    }

    @Transactional
    public PassengerResponse dropOffPassenger(Long rideId, Long applicationId, Long requesterId) {
        Ride ride = findRideById(rideId);
        validateDriver(ride, requesterId);
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new CarpoolException(ErrorCode.RIDE_INVALID_STATUS);
        }
        RidePassenger passenger = findPassenger(rideId, applicationId);
        passenger.dropOff();
        return PassengerResponse.from(passenger);
    }

    private Ride findRideById(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.RIDE_NOT_FOUND));
    }

    private RidePassenger findPassenger(Long rideId, Long applicationId) {
        return ridePassengerRepository.findByRideIdAndApplicationId(rideId, applicationId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.RIDE_PASSENGER_NOT_FOUND));
    }

    private void validateDriver(Ride ride, Long requesterId) {
        if (!ride.getDriverId().equals(requesterId)) {
            throw new CarpoolException(ErrorCode.RIDE_FORBIDDEN);
        }
    }
}
