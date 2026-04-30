# Member 도메인

`com.techeer.carpool.domain.member` — 회원 프로필 조회·수정, 회원 탈퇴 담당

---

## 패키지 구조

```
member/
├── controller/  MemberController
├── dto/         ProfileResponse, ProfileUpdateRequest
├── entity/      Member
├── repository/  MemberRepository
└── service/     MemberProfileService, MemberWithdrawService
```

---

## API

모든 엔드포인트는 JWT 인증 필요 (`anyRequest().authenticated()`)

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/v1/members/me` | 내 프로필 조회 |
| GET | `/api/v1/members/{id}` | ID로 프로필 조회 (본인만 가능) |
| PUT | `/api/v1/members/me` | 프로필 수정 (닉네임, 비밀번호) |
| DELETE | `/api/v1/members/me` | 회원 탈퇴 (소프트 삭제) |

---

## Controller

### `MemberController`

**`GET /me`**
- `authentication.getPrincipal()` → `memberId`
- `getProfile(memberId, memberId)` 호출

**`GET /{id}`**
- `authentication.getPrincipal()` → `requesterId`
- `getProfile(requesterId, id)` 호출
- `requesterId != id` 면 `MEMBER_FORBIDDEN(403)` — 본인 프로필만 조회 가능

**`PUT /me`**
- `authentication.getPrincipal()` → `memberId`
- `@Valid ProfileUpdateRequest` 검증 후 `updateProfile(memberId, request)` 호출

**`DELETE /me`**
- `authentication.getPrincipal()` → `memberId`
- `MemberWithdrawService.withdraw(memberId)` 호출

---

## Entity

### `Member` — `SoftDeletableEntity` 상속

테이블: `members`

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | Long | PK, AUTO | - |
| `email` | String(100) | NOT NULL, UNIQUE | 이메일 |
| `password` | String(60) | NOT NULL | BCrypt 해시 |
| `nickname` | String(50) | NOT NULL | 닉네임 |
| `deleted` | boolean | NOT NULL | 소프트 삭제 플래그 (상속) |
| `createdAt` | LocalDateTime | NOT NULL | 생성 시각 (상속) |
| `updatedAt` | LocalDateTime | NOT NULL | 수정 시각 (상속) |

**비즈니스 메서드**

| 메서드 | 설명 |
|--------|------|
| `updateNickname(String)` | 닉네임 변경 |
| `updatePassword(String)` | 인코딩된 비밀번호로 교체 |
| `withdraw()` | `delete()` 위임 → `deleted = true` |

---

## Repository

### `MemberRepository`

| 메서드 | 용도 |
|--------|------|
| `findByEmail(String)` | 이메일로 회원 조회 |
| `findByIdAndDeletedFalse(Long)` | ID로 활성 회원 조회 |
| `existsByEmail(String)` | 이메일 중복 확인 |

---

## Service

### `MemberProfileService`

**`getProfile(Long requesterId, Long memberId)`**
1. `requesterId != memberId` — `MEMBER_FORBIDDEN(403)`
2. `findByIdAndDeletedFalse()` — 없으면 `MEMBER_NOT_FOUND(404)`
3. `ProfileResponse.from(member)` 반환

**`updateProfile(Long memberId, ProfileUpdateRequest request)`**
- `nickname` 존재 시: `member.updateNickname()`
- `newPassword` 존재 시:
  - `currentPassword` 없거나 불일치 → `INVALID_CREDENTIALS(401)`
  - `member.updatePassword(BCrypt 해싱)`
- 변경 감지로 자동 저장 (`@Transactional`)

### `MemberWithdrawService`

**`withdraw(Long memberId)`**
1. `findByIdAndDeletedFalse()` — 없으면 `MEMBER_NOT_FOUND(404)`
2. `member.withdraw()` — `deleted = true`
3. `refreshTokenRepository.deleteByMemberId()` — Refresh Token 삭제

---

## DTO

### `ProfileResponse`

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | 회원 ID |
| `email` | String | 이메일 |
| `nickname` | String | 닉네임 |
| `createdAt` | LocalDateTime | 가입 시각 |

### `ProfileUpdateRequest`

| 필드 | 타입 | 설명 |
|------|------|------|
| `nickname` | String | 변경할 닉네임 (nullable) |
| `currentPassword` | String | 현재 비밀번호 (비밀번호 변경 시 필요) |
| `newPassword` | String | 변경할 비밀번호 (nullable) |
