# 📝 README.md (Techeer 2026 Team-C)

이 프로젝트는 **실시간 카풀 매칭 서비스**의 백엔드 저장소입니다.  
팀원들이 로컬 환경에서 동일한 설정으로 빠르게 개발을 시작할 수 있도록 가이드를 제공합니다.

---

## 🛠 Tech Stack
* **Language:** Java 17
* **Framework:** Spring Boot 3.4.x
* **Database:** PostgreSQL 15 (Docker 컨테이너)
* **Auth:** Google OAuth2 Client
* **API Spec:** Swagger (SpringDoc)

---

## 🚀 시작하기 (Quick Start)

아래 순서대로 설정을 진행하면 즉시 개발 및 테스트가 가능합니다.

### 1. 환경 변수 설정 (`.env`)
보안을 위해 구글 API 키와 DB 비밀번호는 깃허브에 올리지 않습니다.  
`backend/` 폴더 내에 `.env` 파일을 생성하고 아래 내용을 입력하세요.

> **Pro-Tip:** 실제 키 값은 팀 단톡방의 공지사항을 확인하여 채워 넣어 주세요.

```text
# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here

# Database (Docker용 초기 세팅값)
DB_USERNAME=user
DB_PASSWORD=password
```

### 2. 데이터베이스 실행 (Docker)
로컬 PC에 PostgreSQL을 직접 설치할 필요가 없습니다. **Docker Desktop**이 실행 중인 상태에서 프로젝트 루트 폴더(최상위)의 터미널에 아래 명령어를 입력하세요.

```bash
docker-compose up -d
```


* **컨테이너 확인:** Docker Desktop 화면에서 `carpool-db`가 초록색(Running)인지 확인합니다.

### 3. 애플리케이션 실행
1. IntelliJ에서 `backend` 폴더를 프로젝트로 오픈합니다.
2. `src/main/java/com/techeer/carpool/CarpoolApplication.java`를 실행합니다.
3. 콘솔에 `Started CarpoolApplication` 문구가 뜨면 성공입니다.

---

## 🔗 주요 주소 (Endpoints)
* **로그인 테스트:** `http://localhost:8080/login`  
  (구글 로그인 버튼이 정상적으로 뜨는지 확인하세요.)
* **API 명세서 (Swagger):** `http://localhost:8080/swagger-ui/index.html`  
  (프론트엔드와 협업 시 이 문서를 기준으로 작업합니다.)



---

## 📂 프로젝트 구조 (Structure)
```text
techeer-2026-teamproject-C/
├── docker-compose.yml   # DB 인프라 설정 (PostgreSQL 15)
├── README.md            # 현재 문서
└── backend/             # 스프링 부트 프로젝트 폴더
    ├── .env             # (로컬에서 직접 생성 필요) 환경 변수 파일
    ├── .env.sample      # 환경 변수 작성 가이드 샘플
    ├── build.gradle     # 의존성 관리
    └── src/             # 소스 코드
```

---

## ⚠️ 주의사항
1. **보안:** `.env` 파일은 절대 깃허브에 `Push`하지 마세요. (이미 `.gitignore`에 등록되어 있습니다.)
2. **DB 데이터:** 도커를 껐다 켜도 데이터는 `postgres_data/` 폴더에 보존됩니다. 만약 DB를 완전히 초기화하고 싶다면 해당 폴더를 삭제하고 다시 `docker-compose up`을 하세요.
3. **포트 충돌:** 만약 `5432` 포트가 이미 사용 중이라는 에러가 뜨면, 로컬에 설치된 기존 PostgreSQL 서비스를 종료해야 합니다.

