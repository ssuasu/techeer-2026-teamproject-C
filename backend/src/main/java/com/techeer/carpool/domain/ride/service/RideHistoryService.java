package com.techeer.carpool.domain.ride.service;

import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.domain.review.entity.Review;
import com.techeer.carpool.domain.review.repository.ReviewRepository;
import com.techeer.carpool.domain.ride.dto.PassengerHistoryInfo;
import com.techeer.carpool.domain.ride.dto.RideDetailHistoryResponse;
import com.techeer.carpool.domain.ride.dto.RideHistoryResponse;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideHistoryService {

    private final RideRepository rideRepository;
    private final RidePassengerRepository ridePassengerRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    public List<RideHistoryResponse> getMyHistory(Long memberId) {
        List<Ride> driverRides = rideRepository.findAllByDriverIdAndStatusOrderByCompletedAtDesc(memberId, RideStatus.COMPLETED);
        List<RidePassenger> passengerRecords = ridePassengerRepository.findByPassengerIdAndRideStatus(memberId, RideStatus.COMPLETED);

        Set<Long> postIds = new java.util.HashSet<>();
        driverRides.forEach(r -> postIds.add(r.getPostId()));
        passengerRecords.forEach(rp -> postIds.add(rp.getRide().getPostId()));

        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        List<RideHistoryResponse> result = new ArrayList<>();
        driverRides.forEach(ride ->
                result.add(RideHistoryResponse.from(ride, postMap.get(ride.getPostId()), "DRIVER")));
        passengerRecords.forEach(rp ->
                result.add(RideHistoryResponse.from(rp.getRide(), postMap.get(rp.getRide().getPostId()), "PASSENGER")));

        result.sort(Comparator.comparing(RideHistoryResponse::getCompletedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return result;
    }

    public RideDetailHistoryResponse getHistoryDetail(Long rideId, Long memberId) {
        Ride ride = rideRepository.findByIdWithPassengers(rideId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.RIDE_NOT_FOUND));

        boolean isDriver = ride.getDriverId().equals(memberId);
        boolean isPassenger = ride.getPassengers().stream()
                .anyMatch(rp -> rp.getPassengerId().equals(memberId));

        if (!isDriver && !isPassenger) {
            throw new CarpoolException(ErrorCode.RIDE_FORBIDDEN);
        }

        Post post = postRepository.findById(ride.getPostId()).orElse(null);

        Set<Long> reviewerIds = reviewRepository.findAllByRideId(rideId).stream()
                .map(Review::getReviewerId)
                .collect(Collectors.toSet());

        List<Long> passengerIds = ride.getPassengers().stream()
                .map(RidePassenger::getPassengerId)
                .toList();
        Map<Long, String> nicknameMap = memberRepository.findAllById(passengerIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        List<PassengerHistoryInfo> passengerInfos = ride.getPassengers().stream()
                .map(rp -> PassengerHistoryInfo.builder()
                        .passengerId(rp.getPassengerId())
                        .nickname(nicknameMap.getOrDefault(rp.getPassengerId(), "알 수 없음"))
                        .status(rp.getStatus())
                        .boardedAt(rp.getBoardedAt())
                        .droppedOffAt(rp.getDroppedOffAt())
                        .paymentStatus(rp.getPaymentStatus())
                        .hasReviewed(reviewerIds.contains(rp.getPassengerId()))
                        .build())
                .toList();

        return RideDetailHistoryResponse.builder()
                .departureTime(post != null ? post.getDepartureTime() : null)
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .departureLocation(post != null ? post.getDepartureLocation() : null)
                .departureLat(post != null ? post.getDepartureLat() : null)
                .departureLng(post != null ? post.getDepartureLng() : null)
                .destinationLocation(post != null ? post.getDestinationLocation() : null)
                .destinationLat(post != null ? post.getDestinationLat() : null)
                .destinationLng(post != null ? post.getDestinationLng() : null)
                .price(post != null ? post.getPrice() : null)
                .passengers(passengerInfos)
                .build();
    }
}
