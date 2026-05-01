# Post 도메인

`com.techeer.carpool.domain.post` — 카풀 모집 게시글 CRUD 담당

---

## 패키지 구조

```
post/
├── controller/  PostController
├── dto/         PostCreateRequest, PostUpdateRequest, PostResponse
├── entity/      Post, PostStatus
├── repository/  PostRepository
└── service/     PostCreateService, PostReadService, PostUpdateService, PostDeleteService
```

---

## API

모든 엔드포인트는 JWT 인증 필요

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/v1/posts` | 게시글 생성 |
| GET | `/api/v1/posts` | 전체 목록 조회 (최신순) |
| GET | `/api/v1/posts/{id}` | 단건 조회 |
| PUT | `/api/v1/posts/{id}` | 게시글 수정 (작성자만) |
| DELETE | `/api/v1/posts/{id}` | 게시글 삭제 — 소프트 삭제 (작성자만) |

---

## Controller

### `PostController`

**`POST /`** — `authentication.getPrincipal()` → `memberId` → `createPost(request, memberId)` → `201 Created`

**`GET /`** — 인증 필요, `getAllPosts()` → 최신순 목록

**`GET /{id}`** — `getPostById(id)`

**`PUT /{id}`** — `authentication.getPrincipal()` → `memberId`, 작성자 불일치 시 `POST_FORBIDDEN(403)`

**`DELETE /{id}`** — `authentication.getPrincipal()` → `memberId`, 소프트 삭제

---

## Entity

### `Post` — `SoftDeletableEntity` 상속

테이블: `posts`

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | Long | PK, AUTO | - |
| `memberId` | Long | NOT NULL | 작성자 ID |
| `title` | String(100) | NOT NULL | 제목 |
| `departureLocation` | String(100) | NOT NULL | 출발지 이름 |
| `departureLat` | Double | - | 출발지 위도 |
| `departureLng` | Double | - | 출발지 경도 |
| `destinationLocation` | String(100) | NOT NULL | 목적지 이름 |
| `destinationLat` | Double | - | 목적지 위도 |
| `destinationLng` | Double | - | 목적지 경도 |
| `departureTime` | LocalDateTime | NOT NULL | 출발 시각 |
| `maxPassengers` | int | NOT NULL | 최대 탑승 인원 |
| `currentPassengers` | int | NOT NULL | 현재 탑승 신청 인원 |
| `status` | PostStatus | NOT NULL | 게시글 상태 (기본: OPEN) |
| `description` | TEXT | - | 게시글 본문 |
| `autoAccept` | boolean | NOT NULL | 자동 수락 여부 |
| `price` | Integer | - | 금액 |
| `tags` | List\<String\> | - | 태그 목록 (`post_tags` 테이블) |
| `deleted` | boolean | NOT NULL | 소프트 삭제 (상속) |
| `createdAt` | LocalDateTime | NOT NULL | 생성 시각 (상속) |
| `updatedAt` | LocalDateTime | NOT NULL | 수정 시각 (상속) |

**`@PrePersist`** — 저장 전 `status = OPEN`, `currentPassengers = 0` 자동 설정

**비즈니스 메서드**

| 메서드 | 설명 |
|--------|------|
| `isFull()` | `currentPassengers >= maxPassengers` |
| `incrementPassengers()` | `currentPassengers++`, 정원 도달 시 `status = CLOSED` |
| `updateFrom(PostUpdateRequest)` | 모든 수정 가능 필드 일괄 업데이트 |

### `PostStatus`

| 값 | 의미 |
|----|------|
| `OPEN` | 모집 중 (기본값) |
| `CLOSED` | 정원 마감 |
| `CANCELLED` | 취소됨 |

---

## Repository

### `PostRepository`

| 메서드 | 용도 |
|--------|------|
| `findByDeletedFalseOrderByCreatedAtDesc()` | 삭제되지 않은 게시글 최신순 전체 조회 |
| `findByIdAndDeletedFalse(Long)` | ID로 활성 게시글 단건 조회 |

---

## Service

### `PostCreateService`

**`createPost(PostCreateRequest, Long memberId)`**
1. `Post.builder()` 로 엔티티 생성 후 저장
2. `memberRepository.findById()` 로 닉네임 조회
3. `PostResponse.from(saved, nickname)` 반환

### `PostReadService` — `@Transactional(readOnly = true)`

**`getAllPosts()`**
1. `findByDeletedFalseOrderByCreatedAtDesc()` — 전체 조회
2. `memberRepository.findAllById(memberIds)` — 배치 조회로 N+1 방지
3. nicknameMap 구성 후 `PostResponse` 목록 반환

**`getPostById(Long id)`**
1. `findByIdAndDeletedFalse()` — 없으면 `POST_NOT_FOUND(404)`
2. 개별 nickname 조회 후 `PostResponse` 반환

### `PostUpdateService`

**`updatePost(Long id, PostUpdateRequest, Long requesterId)`**
1. `findByIdAndDeletedFalse()` — 없으면 `POST_NOT_FOUND(404)`
2. `memberId != requesterId` — `POST_FORBIDDEN(403)`
3. `post.updateFrom(request)` — 변경 감지로 자동 저장

### `PostDeleteService`

**`deletePost(Long id, Long requesterId)`**
1. `findByIdAndDeletedFalse()` — 없으면 `POST_NOT_FOUND(404)`
2. `memberId != requesterId` — `POST_FORBIDDEN(403)`
3. `post.delete()` — `deleted = true`

---

## DTO

### `PostCreateRequest`

| 필드 | 타입 |
|------|------|
| `title` | String |
| `departureLocation` | String |
| `departureLat`, `departureLng` | Double |
| `destinationLocation` | String |
| `destinationLat`, `destinationLng` | Double |
| `departureTime` | LocalDateTime |
| `maxPassengers` | int |
| `description` | String |
| `autoAccept` | boolean |
| `price` | Integer |
| `tags` | List\<String\> |

### `PostUpdateRequest`

`PostCreateRequest` 필드 + `status(PostStatus)` 추가 — 상태 직접 변경 가능

### `PostResponse`

`PostCreateRequest` 필드 + `id`, `memberId`, `nickname`, `currentPassengers`, `status`, `createdAt`, `updatedAt`
