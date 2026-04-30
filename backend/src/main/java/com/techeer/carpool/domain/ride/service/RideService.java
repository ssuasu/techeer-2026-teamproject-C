package com.techeer.carpool.domain.ride.service;

import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.entity.PostStatus;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.domain.ride.dto.RideDto;
import com.techeer.carpool.domain.ride.entity.Ride;
import com.techeer.carpool.domain.ride.entity.RidePassenger;
import com.techeer.carpool.domain.ride.repository.RidePassengerRepository;
import com.techeer.carpool.domain.ride.repository.RideRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service                    // 이 클래스가 스프링의 서비스 빈임을 선언, 스프링이 자동으로 관리
@RequiredArgsConstructor    // Lombok: final 필드를 받는 생성자 자동 생성 → 생성자 주입 방식으로 의존성 주입
@Transactional(readOnly = true)
// 클래스 전체에 읽기 전용 트랜잭션 적용
// readOnly=true: 조회 전용 메서드에 최적화 (불필요한 변경 감지 생략 → 성능 향상)
// 데이터 변경이 필요한 메서드는 개별적으로 @Transactional을 따로 붙여서 덮어씀
public class RideService {

    private final RideRepository rideRepository;
    private final RidePassengerRepository ridePassengerRepository;
    private final PostRepository postRepository;

    // 운행 생성
    @Transactional
    public RideDto.RideResponse createRide(RideDto.CreateRequest request) {
        Post post = postRepository.findByIdAndDeletedFalse(request.getPostId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));
        if (!post.getMemberId().equals(request.getDriverId())) {
            throw new CarpoolException(ErrorCode.RIDE_FORBIDDEN);
        }
        if (post.getStatus() != PostStatus.OPEN) {
            throw new CarpoolException(ErrorCode.RIDE_INVALID_STATUS);
        }
        Ride ride = Ride.builder()
                .postId(request.getPostId())
                .driverId(request.getDriverId())
                .build();
        return RideDto.RideResponse.from(rideRepository.save(ride));
    }

    // 운행 조회 (readOnly 트랜잭션 그대로 사용)
    public RideDto.RideResponse getRide(Long rideId) {
        return RideDto.RideResponse.from(findRideById(rideId));
    }

    // 운행 시작
    @Transactional
    public RideDto.RideResponse startRide(Long rideId) {
        Ride ride = findRideById(rideId);
        ride.start();  // 엔티티 내부 메서드로 상태 변경 → 트랜잭션 종료 시 DB 자동 반영 (변경 감지)
        return RideDto.RideResponse.from(ride);
    }

    // 운행 종료
    @Transactional
    public RideDto.RideResponse completeRide(Long rideId) {
        Ride ride = findRideById(rideId);
        ride.complete();  // 상태를 COMPLETED로 변경
        return RideDto.RideResponse.from(ride);
    }

    // 운전자 위치 업데이트
    // 브라우저의 Geolocation API가 주기적으로 이 API를 호출해서 위치를 갱신
    @Transactional
    public RideDto.LocationResponse updateLocation(Long rideId, RideDto.LocationUpdateRequest request) {
        Ride ride = findRideById(rideId);
        ride.updateLocation(request.getLatitude(), request.getLongitude());  // 위도/경도 갱신
        return RideDto.LocationResponse.from(ride);
    }

    // 현재 위치 조회 (탑승자가 드라이버 위치를 확인할 때 사용)
    public RideDto.LocationResponse getLocation(Long rideId) {
        return RideDto.LocationResponse.from(findRideById(rideId));
    }

    // 탑승자 목록 조회
    public List<RideDto.PassengerResponse> getPassengers(Long rideId) {
        Ride ride = findRideById(rideId);
        return ride.getPassengers().stream()          // 탑승자 리스트를 스트림으로 변환
                .map(RideDto.PassengerResponse::from)  // 각 RidePassenger를 PassengerResponse DTO로 변환
                .collect(Collectors.toList());         // 다시 리스트로 모음
    }

    // 탑승 확인 (드라이버가 특정 탑승자의 탑승을 확인)
    @Transactional
    public RideDto.PassengerResponse boardPassenger(Long rideId, Long applicationId) {
        RidePassenger passenger = findPassenger(rideId, applicationId);
        passenger.board();  // 상태를 BOARDED로 변경
        return RideDto.PassengerResponse.from(passenger);
    }

    // 하차 확인 (드라이버가 특정 탑승자의 하차를 확인)
    @Transactional
    public RideDto.PassengerResponse dropOffPassenger(Long rideId, Long applicationId) {
        RidePassenger passenger = findPassenger(rideId, applicationId);
        passenger.dropOff();  // 상태를 DROPPED_OFF로 변경
        return RideDto.PassengerResponse.from(passenger);
    }

    // ── 내부 헬퍼 메서드 ───────────────────────────────────
    // 중복 코드를 줄이기 위해 공통 조회 로직을 분리

    // rideId로 Ride 조회, 없으면 404 예외 발생
    private Ride findRideById(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.RIDE_NOT_FOUND));
    }

    private RidePassenger findPassenger(Long rideId, Long applicationId) {
        return ridePassengerRepository.findByRideIdAndApplicationId(rideId, applicationId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.RIDE_PASSENGER_NOT_FOUND));
    }
}