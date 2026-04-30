# Auth 도메인

`com.techeer.carpool.domain.auth` — 회원가입, 로그인, Access Token 재발급 담당

---

## 패키지 구조

```
auth/
├── controller/  AuthController
├── dto/         SignupRequest, LoginRequest, TokenResponse, AuthTokens
├── entity/      RefreshToken
├── repository/  RefreshTokenRepository
└── service/     MemberSignupService, MemberLoginService, TokenReissueService
```

---

## API

`/api/v1/auth/**` — `SecurityConfig`에서 `permitAll()` 처리, 인증 없이 접근 가능

| 메서드 | 엔드포인트 | 설명 | 인증 |
|--------|-----------|------|------|
| POST | `/api/v1/auth/signup` | 이메일 회원가입 | 불필요 |
| POST | `/api/v1/auth/login` | 로그인 | 불필요 |
| POST | `/api/v1/auth/refresh` | Access Token 재발급 | 불필요 (쿠키 기반) |

---

## Controller

### `AuthController`

**`POST /signup`**
- `@Valid SignupRequest` 검증 후 `MemberSignupService.signup()` 호출
- 성공 시 `201 Created`

**`POST /login`**
- `MemberLoginService.login()` → `AuthTokens(accessToken, refreshToken)` 반환
- `accessToken` → 응답 body (`TokenResponse`)
- `refreshToken` → `setRefreshTokenCookie()` 로 쿠키 설정

**`POST /refresh`**
- `extractRefreshTokenCookie()` 로 쿠키에서 refreshToken 추출
- `TokenReissueService.reissue()` → 새 `AuthTokens` 반환
- 새 refreshToken 으로 쿠키 교체

**`setRefreshTokenCookie()`** — Refresh Token 쿠키 설정 규칙:

| 속성 | 값 |
|------|----|
| `HttpOnly` | `true` (JS 접근 불가, XSS 방어) |
| `SameSite` | `Strict` (CSRF 방어) |
| `Secure` | `${jwt.cookie-secure}` — 로컬: `false`, 배포: `true` |
| `Path` | `/api/v1/auth` (재발급 엔드포인트에서만 쿠키 전송) |
| `MaxAge` | `refreshTokenExpiration` 초 |

**`extractRefreshTokenCookie()`** — `request.getCookies()` null 체크 후 `refreshToken` 쿠키 탐색, 없으면 `INVALID_TOKEN` 예외

---

## Entity

### `RefreshToken`

테이블: `refresh_tokens`

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | Long | PK, AUTO | - |
| `memberId` | Long | NOT NULL | 회원 ID (FK 없이 Long) |
| `token` | String(512) | NOT NULL, UNIQUE | Refresh Token 값 |
| `expiresAt` | LocalDateTime | - | 만료 시각 |

**`rotate(newToken, newExpiresAt)`** — 레코드 삭제 없이 token/expiresAt 교체 → Refresh Token Rotation

> 회원 1명당 레코드 1개 유지. 로그인 시 기존 것 삭제 후 신규 저장, 재발급 시 `rotate()`

---

## Repository

### `RefreshTokenRepository`

| 메서드 | 용도 |
|--------|------|
| `findByToken(String)` | 토큰 문자열로 조회 |
| `deleteByMemberId(Long)` | 로그인·탈퇴 시 기존 토큰 삭제 |

---

## Service

### `MemberSignupService`

**`signup(SignupRequest)`**
1. `existsByEmail()` — 이메일 중복이면 `EMAIL_DUPLICATE(409)`
2. BCrypt 해싱
3. `Member` 생성 후 저장

### `MemberLoginService`

**`login(LoginRequest)`**
1. `findByEmail()` — 없거나 탈퇴 회원이면 `MEMBER_NOT_FOUND(404)`
2. `passwordEncoder.matches()` — 불일치 시 `INVALID_CREDENTIALS(401)`
3. Access Token, Refresh Token 생성
4. `deleteByMemberId()` → 기존 Refresh Token 삭제
5. 새 `RefreshToken` 저장 후 `AuthTokens` 반환

### `TokenReissueService`

**`reissue(String refreshTokenValue)`**
1. `validateToken()` — 유효하지 않으면 `INVALID_TOKEN(401)`
2. `findByToken()` — DB에 없으면 `INVALID_TOKEN(401)`
3. 새 Access Token, Refresh Token 생성
4. `refreshToken.rotate()` — DB 토큰 교체
5. `AuthTokens` 반환

---

## DTO

| 클래스 | 방향 | 필드 |
|--------|------|------|
| `SignupRequest` | → | `email`(@Email), `password`(min 8), `nickname`(max 50) |
| `LoginRequest` | → | `email`(@Email), `password` |
| `TokenResponse` | ← | `accessToken` |
| `AuthTokens` | 내부 | `record(accessToken, refreshToken)` |
