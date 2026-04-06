# 프로젝트 규칙

## 커밋 규칙
- 작업 단위마다 커밋을 찍을 것
- 커밋 메시지는 명확하게 작성할 것
- 기능 추가, 버그 수정, 리팩토링을 구분해서 커밋할 것

### 커밋 메시지 prefix
- `feat:` 새로운 기능 추가
- `fix:` 버그 수정
- `refactor:` 코드 리팩토링 (기능 변경 없음)
- `chore:` 빌드 설정, 의존성, 환경 설정 변경
- `docs:` 문서 수정
- `test:` 테스트 코드 추가/수정

---

## 프로젝트 현황

### 기술 스택
- Java 17, Spring Boot 4.0.4, Spring Security 7
- PostgreSQL 15 (Docker), H2 (테스트)
- JWT (JJWT 0.12.6), BCrypt
- Lombok, Validation

### 패키지 구조
```
com.techeer.carpool
├── domain/
│   ├── member/                  # 회원 도메인
│   │   ├── controller/          # AuthController
│   │   ├── dto/                 # SignupRequest, LoginRequest, TokenResponse, AuthTokens
│   │   ├── entity/              # Member, RefreshToken
│   │   ├── repository/          # MemberRepository, RefreshTokenRepository
│   │   └── service/             # MemberSignupService, MemberLoginService,
│   │                            # TokenReissueService, MemberWithdrawService
│   └── post/                    # 게시글 도메인 (CRUD 완성)
│       ├── controller/          # PostController
│       ├── dto/
│       ├── entity/              # Post, PostStatus
│       ├── repository/
│       └── service/             # PostCreate/Read/Update/DeleteService
└── global/
    ├── common/                  # ApiResponse<T>
    ├── config/                  # SecurityConfig, PasswordConfig
    ├── exception/               # CarpoolException, ErrorCode, GlobalExceptionHandler
    ├── init/                    # LocalDataInitializer (@Profile("local"))
    └── jwt/                     # JwtTokenProvider, JwtAuthenticationFilter
```

### 완성된 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 이메일 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 (accessToken 반환, refreshToken 쿠키) |
| POST | `/api/v1/auth/refresh` | Access Token 재발급 (쿠키 기반) |
| POST | `/api/v1/posts` | 게시글 생성 |
| GET | `/api/v1/posts` | 전체 목록 조회 |
| GET | `/api/v1/posts/{id}` | 단건 조회 |
| PUT | `/api/v1/posts/{id}` | 게시글 수정 |
| DELETE | `/api/v1/posts/{id}` | 게시글 삭제 (소프트) |

### 주요 설계 결정
- Refresh Token은 HttpOnly 쿠키 (SameSite=Strict, 로컬 Secure=false / 배포 Secure=true)
- Refresh Token Rotation: 재발급 시 DB 토큰 교체
- PasswordEncoder는 SecurityConfig와 순환 의존성 방지를 위해 PasswordConfig로 분리
- 모든 API 응답은 ApiResponse<T> { message, data } 형식
- 로컬 프로파일 기본 활성화, 앱 실행 시 테스트 계정 자동 생성

### 인증이 필요한 API 개발 시
```java
public ResponseEntity<?> example(Authentication authentication) {
    Long memberId = (Long) authentication.getPrincipal();
}
```

### 에러 코드
| 코드 | 상황 |
|------|------|
| `POST_001` | 게시글 없음 (404) |
| `COMMON_001` | 잘못된 입력값 (400) |
| `AUTH_001` | 이메일 중복 (409) |
| `AUTH_002` | 사용자 없음 (404) |
| `AUTH_003` | 이메일/비밀번호 불일치 (401) |
| `AUTH_004` | 유효하지 않은 토큰 (401) |
| `AUTH_005` | 만료된 토큰 (401) |
