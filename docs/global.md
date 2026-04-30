# Global 패키지

`com.techeer.carpool.global` — 전 도메인에서 공유하는 공통 설정, 유틸리티, 인프라 담당

---

## 패키지 구조

```
global/
├── common/
│   ├── ApiResponse.java
│   └── entity/
│       ├── BaseEntity.java
│       └── SoftDeletableEntity.java
├── config/
│   ├── SecurityConfig.java
│   └── PasswordConfig.java
├── exception/
│   ├── CarpoolException.java
│   ├── ErrorCode.java
│   └── GlobalExceptionHandler.java
├── init/
│   └── LocalDataInitializer.java
└── jwt/
    ├── JwtTokenProvider.java
    └── JwtAuthenticationFilter.java
```

---

## common

### `ApiResponse<T>`

모든 API 응답의 공통 래퍼.

```json
{ "message": "...", "data": { ... } }
```

| 팩토리 메서드 | 용도 |
|-------------|------|
| `of(String message, T data)` | 데이터 포함 응답 |
| `of(String message)` | 메시지만 있는 응답 (data = null) |

### `BaseEntity`

| 필드 | 어노테이션 | 설명 |
|------|-----------|------|
| `createdAt` | `@CreationTimestamp`, updatable=false | 생성 시각 자동 설정 |
| `updatedAt` | `@UpdateTimestamp` | 수정 시각 자동 갱신 |

상속 대상: `Application`, `Ride`, `RidePassenger`, `RefreshToken`

### `SoftDeletableEntity` — `BaseEntity` 상속

| 필드 | 기본값 | 설명 |
|------|--------|------|
| `deleted` | `false` | 소프트 삭제 플래그 |

**`delete()`** — `deleted = true`

상속 대상: `Member`, `Post`

> `Comment`는 `SoftDeletableEntity`를 상속하지 않고 `deleted` 필드를 직접 구현함 (코드 불일치)

---

## config

### `SecurityConfig`

**인증 규칙**

| 경로 | 접근 |
|------|------|
| `/api/v1/auth/**` | `permitAll()` — 인증 불필요 |
| 그 외 모든 경로 | `authenticated()` — JWT 필요 |

**기타 설정**

| 항목 | 설정 |
|------|------|
| CSRF | 비활성화 |
| Session | `STATELESS` |
| CORS | `http://localhost:5173` 허용, 모든 메서드/헤더, `allowCredentials=true` |
| Filter | `JwtAuthenticationFilter` → `UsernamePasswordAuthenticationFilter` 앞에 추가 |

### `PasswordConfig`

`BCryptPasswordEncoder` 빈 등록.

`SecurityConfig`와의 순환 의존성 방지를 위해 별도 설정 클래스로 분리.

---

## exception

### `CarpoolException`

`RuntimeException` 상속. `ErrorCode`를 감싸는 커스텀 예외.

```java
throw new CarpoolException(ErrorCode.POST_NOT_FOUND);
```

### `ErrorCode`

| 코드 | 상수명 | HTTP | 메시지 |
|------|--------|------|--------|
| POST_001 | `POST_NOT_FOUND` | 404 | 게시글을 찾을 수 없습니다. |
| POST_002 | `POST_FORBIDDEN` | 403 | 게시글 수정/삭제 권한이 없습니다. |
| COMMENT_001 | `COMMENT_NOT_FOUND` | 404 | 댓글을 찾을 수 없습니다. |
| COMMENT_002 | `COMMENT_FORBIDDEN` | 403 | 댓글 삭제 권한이 없습니다. |
| APPLICATION_001 | `APPLICATION_NOT_FOUND` | 404 | 신청을 찾을 수 없습니다. |
| APPLICATION_002 | `APPLICATION_DUPLICATE` | 409 | 이미 신청한 게시글입니다. |
| APPLICATION_003 | `APPLICATION_SELF` | 400 | 본인 게시글에는 신청할 수 없습니다. |
| APPLICATION_004 | `APPLICATION_FORBIDDEN` | 403 | 신청 수락/거절 권한이 없습니다. |
| APPLICATION_005 | `APPLICATION_POST_FULL` | 409 | 정원이 가득 찬 게시글입니다. |
| APPLICATION_006 | `APPLICATION_ALREADY_PROCESSED` | 409 | 이미 처리된 신청입니다. |
| COMMON_001 | `INVALID_INPUT` | 400 | 잘못된 입력값입니다. |
| AUTH_001 | `EMAIL_DUPLICATE` | 409 | 이미 사용 중인 이메일입니다. |
| AUTH_002 | `MEMBER_NOT_FOUND` | 404 | 사용자를 찾을 수 없습니다. |
| AUTH_003 | `INVALID_CREDENTIALS` | 401 | 이메일 또는 비밀번호가 올바르지 않습니다. |
| AUTH_004 | `INVALID_TOKEN` | 401 | 유효하지 않은 토큰입니다. |
| AUTH_005 | `EXPIRED_TOKEN` | 401 | 만료된 토큰입니다. |
| RIDE_001 | `RIDE_NOT_FOUND` | 404 | 운행을 찾을 수 없습니다. |
| RIDE_002 | `RIDE_FORBIDDEN` | 403 | 운행 제어 권한이 없습니다. |
| RIDE_003 | `RIDE_INVALID_STATUS` | 409 | 현재 상태에서 허용되지 않는 작업입니다. |
| RIDE_004 | `RIDE_PASSENGER_NOT_FOUND` | 404 | 탑승자를 찾을 수 없습니다. |
| MEMBER_001 | `MEMBER_FORBIDDEN` | 403 | 본인의 프로필만 조회할 수 있습니다. |

### `GlobalExceptionHandler`

`@RestControllerAdvice` — 전역 예외 처리

**에러 응답 형식**
```json
{ "code": "POST_001", "message": "게시글을 찾을 수 없습니다." }
```

| 핸들러 | 대상 | 응답 |
|--------|------|------|
| `handleCarpoolException` | `CarpoolException` | `ErrorCode`의 status + code + message |
| `handleValidationException` | `MethodArgumentNotValidException` | 400, COMMON_001, 첫 번째 필드 에러 메시지 |
| `handleGeneralException` | `Exception` (fallback) | 500, SERVER_ERROR |

---

## init

### `LocalDataInitializer`

`@Profile("local")` — 로컬 환경에서만 동작 (`CommandLineRunner`)

앱 시작 시 자동 실행:
- `test@carpool.com` / `password1234` / 테스트유저
- `admin@carpool.com` / `admin1234!` / 관리자
- 게시글 3개 (강남→판교, 홍대→여의도, 서울역→수원역) + 각 게시글에 댓글 3개

`createMemberIfNotExists()` — 이미 존재하면 재생성하지 않음 (멱등성 보장)

---

## jwt

### `JwtTokenProvider`

설정값 (`application.yml`):

| 프로퍼티 | 설명 |
|---------|------|
| `jwt.secret` | HMAC-SHA 서명 키 (환경변수 `${JWT_SECRET}` 권장) |
| `jwt.access-token-expiration` | Access Token 만료 시간 (ms) |
| `jwt.refresh-token-expiration` | Refresh Token 만료 시간 (ms) |
| `jwt.cookie-secure` | Refresh Token 쿠키 Secure 속성 (로컬: false) |

**주요 메서드**

| 메서드 | 설명 |
|--------|------|
| `createAccessToken(Long memberId)` | Access Token 생성 |
| `createRefreshToken(Long memberId)` | Refresh Token 생성 |
| `validateToken(String)` | 토큰 서명·만료 검증, boolean 반환 |
| `getMemberIdFromToken(String)` | 토큰 payload의 `memberId` 클레임 추출 |
| `getRefreshTokenExpiresAt()` | `LocalDateTime.now() + refreshExpiration` |
| `getRefreshTokenExpirationSeconds()` | 쿠키 maxAge 설정용 초 단위 값 |

토큰 payload: `{ "memberId": Long, "iat": ..., "exp": ... }`

### `JwtAuthenticationFilter`

`OncePerRequestFilter` 상속 — 요청당 1회 실행

**처리 흐름**:
1. `Authorization: Bearer <token>` 헤더에서 토큰 추출
2. `jwtTokenProvider.validateToken()` — 유효하면
3. `getMemberIdFromToken()` → `memberId` 추출
4. `UsernamePasswordAuthenticationToken(memberId, null, [])` 생성
5. `SecurityContextHolder`에 저장
6. `filterChain.doFilter()` — 다음 필터로 전달

컨트롤러에서 `authentication.getPrincipal()` 호출 시 `Long memberId` 반환.
