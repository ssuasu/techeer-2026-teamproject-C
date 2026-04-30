# Application 도메인

`com.techeer.carpool.domain.application` — 카풀 탑승 신청 및 수락/거절 담당

---

## 패키지 구조

```
application/
├── controller/  ApplicationController
├── dto/         ApplicationResponse
├── entity/      Application, ApplicationStatus
├── repository/  ApplicationRepository
└── service/     ApplicationCreateService, ApplicationReadService, ApplicationStatusService
```

---

## API

모든 엔드포인트는 JWT 인증 필요

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/v1/posts/{postId}/applications` | 카풀 신청 |
| GET | `/api/v1/applications/me` | 내 신청 내역 조회 |
| GET | `/api/v1/posts/{postId}/applications` | 게시글 신청 목록 조회 (작성자만) |
| PATCH | `/api/v1/applications/{id}/accept` | 신청 수락 (게시글 작성자만) |
| PATCH | `/api/v1/applications/{id}/reject` | 신청 거절 (게시글 작성자만) |

---

## Controller

### `ApplicationController`

모든 메서드는 `authentication.getPrincipal()` → `memberId` 추출 후 서비스에 전달.

---

## Entity

### `Application` — `BaseEntity` 상속

테이블: `applications`

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | Long | PK, AUTO | - |
| `postId` | Long | NOT NULL | 신청한 게시글 ID |
| `applicantId` | Long | NOT NULL | 신청자 회원 ID |
| `status` | ApplicationStatus | NOT NULL | 신청 상태 (기본: PENDING) |
| `createdAt` | LocalDateTime | NOT NULL | 생성 시각 (상속) |
| `updatedAt` | LocalDateTime | NOT NULL | 수정 시각 (상속) |

**유니크 제약**: `(post_id, applicant_id)` — 동일 게시글에 중복 신청 불가 (DB 레벨)

**비즈니스 메서드**

| 메서드 | 설명 |
|--------|------|
| `accept()` | `status = ACCEPTED` |
| `reject()` | `status = REJECTED` |

### `ApplicationStatus`

| 값 | 의미 |
|----|------|
| `PENDING` | 대기 중 (기본값) |
| `ACCEPTED` | 수락됨 |
| `REJECTED` | 거절됨 |

---

## Repository

### `ApplicationRepository`

| 메서드 | 용도 |
|--------|------|
| `existsByPostIdAndApplicantId(Long, Long)` | 중복 신청 확인 |
| `findByApplicantIdOrderByCreatedAtDesc(Long)` | 내 신청 내역 최신순 조회 |
| `findByPostIdOrderByCreatedAtAsc(Long)` | 게시글 신청 목록 오래된순 조회 |
| `countByPostIdAndStatus(Long, ApplicationStatus)` | 특정 상태 신청 수 집계 |

---

## Service

### `ApplicationCreateService`

**`apply(Long postId, Long applicantId)`**
1. `findByIdAndDeletedFalse()` — 게시글 없으면 `POST_NOT_FOUND(404)`
2. `post.getMemberId() == applicantId` — `APPLICATION_SELF(400)` (본인 게시글 신청 불가)
3. `post.isFull()` — `APPLICATION_POST_FULL(409)`
4. `existsByPostIdAndApplicantId()` — `APPLICATION_DUPLICATE(409)`
5. `Application` 저장 후 `ApplicationResponse` 반환

### `ApplicationReadService`

**`getMyApplications(Long applicantId)`**
- `findByApplicantIdOrderByCreatedAtDesc()` 조회
- 단일 nickname 조회 후 목록 반환

**`getApplicationsByPost(Long postId, Long requesterId)`**
1. 게시글 존재 확인
2. `post.getMemberId() != requesterId` — `APPLICATION_FORBIDDEN(403)`
3. `findByPostIdOrderByCreatedAtAsc()` 조회
4. `memberRepository.findAllById(applicantIds)` — 배치 조회로 N+1 방지
5. nicknameMap 구성 후 목록 반환

### `ApplicationStatusService`

**`accept(Long applicationId, Long requesterId)`**
1. `findPending()` — 없으면 `APPLICATION_NOT_FOUND(404)`, 이미 처리됐으면 `APPLICATION_ALREADY_PROCESSED(409)`
2. 게시글 존재 확인
3. `post.getMemberId() != requesterId` — `APPLICATION_FORBIDDEN(403)`
4. `post.isFull()` — `APPLICATION_POST_FULL(409)`
5. `application.accept()` + `post.incrementPassengers()`
6. `ApplicationResponse` 반환

**`reject(Long applicationId, Long requesterId)`**
1. `findPending()` — 상태 및 존재 확인
2. 게시글 존재·권한 확인
3. `application.reject()`

> **주의**: `accept()` 호출 시 `post.incrementPassengers()`가 `currentPassengers`를 증가시키고 정원 도달 시 `status = CLOSED`로 자동 변경.
> 현재 `Application` 수락 후 `RidePassenger` 생성 로직은 미구현 (ride 도메인 구현 시 연결 필요).

---

## DTO

### `ApplicationResponse`

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 신청 ID |
| `postId` | Long | 게시글 ID |
| `applicantId` | Long | 신청자 ID |
| `applicantNickname` | String | 신청자 닉네임 |
| `status` | ApplicationStatus | 신청 상태 |
| `createdAt` | LocalDateTime | 신청 시각 |
