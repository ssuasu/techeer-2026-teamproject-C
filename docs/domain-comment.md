# Comment 도메인

`com.techeer.carpool.domain.comment` — 게시글 댓글 작성·조회·삭제 담당

---

## 패키지 구조

```
comment/
├── controller/  CommentController
├── dto/         CommentCreateRequest, CommentResponse
├── entity/      Comment
├── repository/  CommentRepository
└── service/     CommentCreateService, CommentReadService, CommentDeleteService
```

---

## API

모든 엔드포인트는 JWT 인증 필요

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/v1/posts/{postId}/comments` | 댓글 작성 |
| GET | `/api/v1/posts/{postId}/comments` | 댓글 목록 조회 |
| DELETE | `/api/v1/comments/{commentId}` | 댓글 삭제 — 소프트 삭제 (작성자만) |

---

## Controller

### `CommentController`

**`POST /posts/{postId}/comments`** — `authentication.getPrincipal()` → `memberId`, `@Valid` 검증 후 `201 Created`

**`GET /posts/{postId}/comments`** — 인증 필요, 삭제되지 않은 댓글 오래된순 반환

**`DELETE /comments/{commentId}`** — `authentication.getPrincipal()` → `memberId`, 작성자 불일치 시 `COMMENT_FORBIDDEN(403)`

---

## Entity

### `Comment`

> `BaseEntity` 상속 없음 — `createdAt` 직접 관리, `SoftDeletableEntity` 미상속

테이블: `comments`

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | Long | PK, AUTO | - |
| `postId` | Long | NOT NULL | 게시글 ID |
| `memberId` | Long | NOT NULL | 작성자 ID |
| `content` | String(500) | NOT NULL | 댓글 내용 |
| `createdAt` | LocalDateTime | NOT NULL, updatable=false | 작성 시각 |
| `deleted` | boolean | NOT NULL | 소프트 삭제 플래그 |

**`@PrePersist`** — `createdAt = now()`, `deleted = false` 자동 설정

**비즈니스 메서드**

| 메서드 | 설명 |
|--------|------|
| `delete()` | `deleted = true` |

---

## Repository

### `CommentRepository`

| 메서드 | 용도 |
|--------|------|
| `findByPostIdAndDeletedFalseOrderByCreatedAtAsc(Long)` | 게시글의 활성 댓글 오래된순 조회 |
| `findByIdAndDeletedFalse(Long)` | ID로 활성 댓글 단건 조회 |

---

## Service

### `CommentCreateService`

**`createComment(Long postId, CommentCreateRequest, Long memberId)`**
1. 게시글 존재 확인 — `POST_NOT_FOUND(404)`
2. `Comment` 생성 및 저장
3. 닉네임 조회 후 `CommentResponse` 반환

### `CommentReadService`

**`getCommentsByPostId(Long postId)`**
1. 게시글 존재 확인 — `POST_NOT_FOUND(404)`
2. `findByPostIdAndDeletedFalseOrderByCreatedAtAsc()` 조회
3. `memberRepository.findAllById(memberIds)` — 배치 조회로 N+1 방지
4. nicknameMap 구성 후 `CommentResponse` 목록 반환

### `CommentDeleteService`

**`deleteComment(Long commentId, Long memberId)`**
1. `findByIdAndDeletedFalse()` — `COMMENT_NOT_FOUND(404)`
2. `comment.getMemberId() != memberId` — `COMMENT_FORBIDDEN(403)`
3. `comment.delete()`

---

## DTO

### `CommentCreateRequest`

| 필드 | 제약 | 설명 |
|------|------|------|
| `content` | @NotBlank, max 500 | 댓글 내용 |

### `CommentResponse`

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 댓글 ID |
| `postId` | Long | 게시글 ID |
| `memberId` | Long | 작성자 ID |
| `nickname` | String | 작성자 닉네임 |
| `content` | String | 댓글 내용 |
| `createdAt` | LocalDateTime | 작성 시각 |
