# 알려진 이슈 & 개선 목록

심각도: 🔴 보안/버그 → 🟡 설계 결함 → 🟢 개선 사항  
상태: ✅ 해결됨 | 🔧 미해결

---

## 🔴 보안

### [SEC-01] ✅ 게시글 생성 시 memberId를 Authentication에서 추출
`PostController`에서 `authentication.getPrincipal()` 으로 memberId 추출. `PostCreateRequest`에 memberId 없음. **해결됨.**

---

### [SEC-02] 🔧 Ride 생성 시 driverId를 Request Body로 받음
**파일:** `RideDto.java (CreateRequest)`

`RideDto.CreateRequest`에 `driverId` 필드가 있어 클라이언트가 타인의 ID로 운행 생성 가능. `RideController`에 `Authentication` 미적용.

**수정 방향:** `CreateRequest`에서 `driverId` 제거, `Authentication`에서 추출.

---

### [SEC-03] ✅ GET /api/v1/posts 인증 필요
비인증 사용자의 게시글 목록/단건 조회 차단. `SecurityConfig`에서 `permitAll()` 제거. **해결됨.**

---

### [SEC-04] ✅ 타인 프로필 조회 차단
`MemberProfileService.getProfile()`에 `requesterId == memberId` 소유권 검증 추가. `MEMBER_FORBIDDEN(403)` 반환. **해결됨.**

---

## 🔴 버그

### [BUG-01] ✅ RideService 예외 500 반환 문제
`EntityNotFoundException`, `IllegalStateException` → `CarpoolException`으로 교체. `RIDE_001~004` 에러 코드 추가. **해결됨.**

---

### [BUG-02] ✅ board/dropOff 시 Ride 상태 검증 없음
`boardPassenger()`, `dropOffPassenger()` 에서 `ride.getStatus() != IN_PROGRESS` 체크 후 `RIDE_INVALID_STATUS(409)` 반환. **해결됨.**

---

### [BUG-03] 🔧 Post.updateFrom() 이 null 로 필드를 덮어씀
**파일:** `Post.java (updateFrom)`

`PostUpdateRequest`의 모든 필드는 nullable 인데, `updateFrom()`은 null 체크 없이 전부 덮어씀. 일부 필드만 전달 시 나머지가 null 로 초기화됨.

**수정 방향:** 각 필드에 null 체크 후 선택적 업데이트.

---

### [BUG-04] 🔧 PostController에 @Valid 누락
**파일:** `PostController.java`

`@RequestBody PostCreateRequest request`, `@RequestBody PostUpdateRequest request` 에 `@Valid` 어노테이션이 없어 DTO 유효성 검사 미실행. `PostCreateRequest`에도 `@NotBlank` 등 검증 어노테이션 없음.

**수정 방향:** `@Valid @RequestBody` 추가, `PostCreateRequest`에 검증 어노테이션 추가.

---

### [BUG-05] 🔧 Application 수락 시 RidePassenger 미생성
**파일:** `ApplicationStatusService.java (accept)`

`application.accept()` + `post.incrementPassengers()` 만 수행. 수락 후 `RidePassenger` 를 생성하는 코드가 없어 `getPassengers()`, `boardPassenger()`, `dropOffPassenger()` API 가 항상 빈 결과 또는 404 반환.

**수정 방향:** Ride 도메인 구현 시 `accept()` 내에서 해당 `postId`의 `Ride`를 찾아 `RidePassenger` 생성.

---

## 🟡 설계 결함

### [DESIGN-01] 🔧 RideController가 ApiResponse 래퍼 미사용
**파일:** `RideController.java`

다른 컨트롤러는 모두 `ApiResponse<T> { message, data }` 형태로 응답하지만 `RideController`는 DTO를 직접 반환.

**수정 방향:** 모든 응답을 `ApiResponse.of(...)` 로 래핑.

---

### [DESIGN-02] 🔧 에러 응답과 성공 응답 형식 불일치
**파일:** `GlobalExceptionHandler.java`

- 성공: `{ "message": "...", "data": { ... } }`
- 에러: `{ "code": "...", "message": "..." }`

**수정 방향:** `ErrorResponse { code, message }` 타입을 정의하거나 에러도 `ApiResponse` 구조에 맞춤.

---

### [DESIGN-03] ✅ MemberWithdrawService 엔드포인트 연결
`MemberController`에 `DELETE /api/v1/members/me` 엔드포인트 추가됨. **해결됨.**

---

### [DESIGN-04] ✅ ErrorCode Ride 관련 코드 추가
`RIDE_001~004`, `MEMBER_001` 추가됨. **해결됨.**

---

### [DESIGN-05] 🔧 RideController에 드라이버 권한 검증 없음
**파일:** `RideController.java`

`start`, `complete`, `location`, `board`, `dropOff` 엔드포인트에 `Authentication` 파라미터가 없어 누구나 타인의 운행 제어 가능.

**수정 방향:** 각 메서드에 `Authentication authentication` 추가, `driverId = (Long) authentication.getPrincipal()` 추출 후 서비스에 전달.

---

### [DESIGN-06] 🔧 Comment가 SoftDeletableEntity 미상속
**파일:** `Comment.java`

`Post`, `Member`는 `SoftDeletableEntity` 상속, `Comment`는 `deleted` 필드와 `delete()` 메서드를 직접 구현. `updatedAt` 없음, `BaseEntity` 상속도 없음.

**수정 방향:** `SoftDeletableEntity` 상속으로 통일.

---

### [DESIGN-07] 🔧 JwtTokenProvider.validateToken() 만료/위조 구분 불가
**파일:** `JwtTokenProvider.java`

`validateToken()`이 boolean 반환. `JwtAuthenticationFilter`에서 `EXPIRED_TOKEN`과 `INVALID_TOKEN`을 구분할 수 없어 클라이언트가 항상 동일한 401 수신.

**수정 방향:** `TokenValidationResult` enum 반환 또는 예외 타입 분기 처리.

---

### [DESIGN-08] 🔧 Application 수락 시 동시성 Race Condition
**파일:** `ApplicationStatusService.java (accept)`

정원 체크 → `incrementPassengers()` 사이에 동시 요청 시 `maxPassengers` 초과 가능.

**수정 방향:** `Post` 엔티티에 `@Version` (낙관적 락) 또는 `@Lock(PESSIMISTIC_WRITE)` 적용.

---

## 🟢 개선 사항

### [IMPROVE-01] 🔧 전체 목록 조회 페이지네이션 없음
`getAllPosts()`가 모든 게시글을 한 번에 반환. `Pageable` 파라미터 추가 필요.

---

### [IMPROVE-02] 🔧 ApplicationStatusService.toResponse() N+1 쿼리
**파일:** `ApplicationStatusService.java`

`memberRepository.findById(applicantId)` 를 매번 개별 호출. 반복 수락/거절 시 N+1 발생.

---

### [IMPROVE-03] 🔧 엔티티 간 관계를 ID(Long)로만 참조
`Post.memberId`, `Ride.postId`, `Ride.driverId` 등을 `@ManyToOne` 대신 Long으로 저장. JOIN 쿼리 불가, 연관 데이터 조회 시 별도 쿼리 필요.

---

### [IMPROVE-04] 🔧 테스트 코드 부재
`CarpoolApplicationTests`, `CommentIntegrationTest` 외에 서비스 단위 테스트 없음. 핵심 서비스(Login, TokenReissue, ApplicationStatus 등)부터 작성 필요.

---

### [IMPROVE-05] 🔧 ProfileUpdateRequest 교차 필드 검증 없음
비밀번호 변경 시 `currentPassword`와 `newPassword`를 함께 제공해야 하는 규칙이 DTO 레벨에서 검증되지 않고 서비스 로직에만 의존.
