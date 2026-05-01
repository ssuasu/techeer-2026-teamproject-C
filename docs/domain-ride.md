# Ride 도메인

`com.techeer.carpool.domain.ride` — 실제 운행 관리 담당 (미완성, 향후 구현 예정)

---

## 패키지 구조

```
ride/
├── controller/  RideController
├── dto/         RideDto (CreateRequest, RideResponse, LocationResponse, PassengerResponse, LocationUpdateRequest)
├── entity/      Ride, RidePassenger, RideStatus, PassengerStatus
├── repository/  RideRepository, RidePassengerRepository
└── service/     RideService
```

---

## API

모든 엔드포인트는 JWT 인증 필요

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/v1/rides` | 운행 생성 |
| GET | `/api/v1/rides/{rideId}` | 운행 단건 조회 |
| POST | `/api/v1/rides/{rideId}/start` | 운행 시작 |
| POST | `/api/v1/rides/{rideId}/complete` | 운행 종료 |
| POST | `/api/v1/rides/{rideId}/location` | 드라이버 위치 업데이트 |
| GET | `/api/v1/rides/{rideId}/location` | 드라이버 현재 위치 조회 |
| GET | `/api/v1/rides/{rideId}/passengers` | 탑승자 목록 조회 |
| POST | `/api/v1/rides/{rideId}/passengers/{applicationId}/board` | 탑승 확인 |
| POST | `/api/v1/rides/{rideId}/passengers/{applicationId}/dropoff` | 하차 확인 |

> **주의**: 현재 Controller에 `Authentication` 미적용. 드라이버 권한 검증 없음. 향후 구현 시 추가 필요.

---

## 상태 흐름

```
Ride:          SCHEDULED → IN_PROGRESS → COMPLETED
RidePassenger: PENDING   → BOARDED     → DROPPED_OFF
```

---

## Entity

### `Ride` — `BaseEntity` 상속

테이블: `rides`

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | Long | PK, AUTO | - |
| `postId` | Long | NOT NULL | 기반 게시글 ID |
| `driverId` | Long | NOT NULL | 드라이버 회원 ID |
| `status` | RideStatus | NOT NULL | 운행 상태 (기본: SCHEDULED) |
| `currentLatitude` | Double | - | 드라이버 현재 위도 |
| `currentLongitude` | Double | - | 드라이버 현재 경도 |
| `startedAt` | LocalDateTime | - | 운행 시작 시각 |
| `completedAt` | LocalDateTime | - | 운행 종료 시각 |
| `passengers` | List\<RidePassenger\> | CASCADE ALL | 탑승자 목록 (1:N) |
| `createdAt` | LocalDateTime | NOT NULL | 생성 시각 (상속) |
| `updatedAt` | LocalDateTime | NOT NULL | 수정 시각 (상속) |

**비즈니스 메서드**

| 메서드 | 선행 조건 | 동작 |
|--------|----------|------|
| `start()` | `status == SCHEDULED` | `status = IN_PROGRESS`, `startedAt = now()` |
| `complete()` | `status == IN_PROGRESS` | `status = COMPLETED`, `completedAt = now()` |
| `updateLocation(lat, lng)` | `status == IN_PROGRESS` | 위도/경도 갱신 |

> 조건 불충족 시 `IllegalStateException` throw (서비스 레이어의 `RIDE_INVALID_STATUS` 체크가 선행됨)

### `RidePassenger` — `BaseEntity` 상속

테이블: `ride_passengers`

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | Long | PK, AUTO | - |
| `ride` | Ride | FK ride_id, NOT NULL | 소속 운행 (ManyToOne LAZY) |
| `applicationId` | Long | NOT NULL | 관련 신청 ID |
| `passengerId` | Long | NOT NULL | 탑승자 회원 ID |
| `status` | PassengerStatus | NOT NULL | 탑승 상태 (기본: PENDING) |
| `boardedAt` | LocalDateTime | - | 탑승 확인 시각 |
| `droppedOffAt` | LocalDateTime | - | 하차 확인 시각 |

**비즈니스 메서드**

| 메서드 | 선행 조건 | 동작 |
|--------|----------|------|
| `board()` | `status == PENDING` | `status = BOARDED`, `boardedAt = now()` |
| `dropOff()` | `status == BOARDED` | `status = DROPPED_OFF`, `droppedOffAt = now()` |

### `RideStatus`

| 값 | 의미 |
|----|------|
| `SCHEDULED` | 예정 (기본값) |
| `IN_PROGRESS` | 운행 중 |
| `COMPLETED` | 완료 |

### `PassengerStatus`

| 값 | 의미 |
|----|------|
| `PENDING` | 대기 (기본값) |
| `BOARDED` | 탑승 확인 |
| `DROPPED_OFF` | 하차 확인 |

---

## Repository

### `RideRepository`

| 메서드 | 용도 |
|--------|------|
| `findByDriverIdOrderByCreatedAtDesc(Long)` | 드라이버의 운행 목록 최신순 조회 |

### `RidePassengerRepository`

| 메서드 | 용도 |
|--------|------|
| `findByRideIdAndApplicationId(Long, Long)` | rideId + applicationId로 탑승자 조회 |
| `findByPassengerIdOrderByCreatedAtDesc(Long)` | 탑승자의 탑승 내역 최신순 조회 |

---

## Service

### `RideService` — `@Transactional(readOnly = true)`

**`createRide(RideDto.CreateRequest)`**
1. `findByIdAndDeletedFalse(postId)` — `POST_NOT_FOUND(404)`
2. `post.getMemberId() != request.getDriverId()` — `RIDE_FORBIDDEN(403)`
3. `post.getStatus() != OPEN` — `RIDE_INVALID_STATUS(409)`
4. `Ride` 생성 후 저장

**`getRide(Long rideId)`** — `RIDE_NOT_FOUND(404)`

**`startRide(Long rideId)`** — Ride 조회 후 `ride.start()`

**`completeRide(Long rideId)`** — Ride 조회 후 `ride.complete()`

**`updateLocation(Long rideId, LocationUpdateRequest)`** — `ride.updateLocation(lat, lng)`

**`getLocation(Long rideId)`** — 현재 위도/경도 반환

**`getPassengers(Long rideId)`** — 탑승자 목록 반환

**`boardPassenger(Long rideId, Long applicationId)`**
1. `ride.getStatus() != IN_PROGRESS` — `RIDE_INVALID_STATUS(409)`
2. 탑승자 조회 — `RIDE_PASSENGER_NOT_FOUND(404)`
3. `passenger.board()`

**`dropOffPassenger(Long rideId, Long applicationId)`**
1. `ride.getStatus() != IN_PROGRESS` — `RIDE_INVALID_STATUS(409)`
2. 탑승자 조회 — `RIDE_PASSENGER_NOT_FOUND(404)`
3. `passenger.dropOff()`

---

## DTO

### `RideDto` (중첩 클래스 모음)

| 클래스 | 방향 | 주요 필드 |
|--------|------|----------|
| `CreateRequest` | → | `postId`, `driverId` |
| `LocationUpdateRequest` | → | `latitude`, `longitude` |
| `RideResponse` | ← | `id`, `postId`, `driverId`, `status`, `currentLatitude`, `currentLongitude`, `startedAt`, `completedAt` |
| `LocationResponse` | ← | `rideId`, `driverLatitude`, `driverLongitude` |
| `PassengerResponse` | ← | `id`, `applicationId`, `passengerId`, `status`, `boardedAt`, `droppedOffAt` |

---

## 미구현 / 향후 과제

| 항목 | 설명 |
|------|------|
| Application → RidePassenger 연결 | `ApplicationStatusService.accept()` 시 `RidePassenger` 자동 생성 로직 없음 |
| Controller Authentication 적용 | 드라이버 권한 검증을 위해 `Authentication` 파라미터 추가 필요 |
| 내 운행 목록 API | `GET /rides/me` (드라이버 기준), `GET /rides/me/passenger` (탑승자 기준) 미구현 |
