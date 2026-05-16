# 모니터링 설정 가이드

## 구성 개요

| 구성 요소 | 역할 |
|-----------|------|
| Spring Boot Actuator | 애플리케이션 메트릭 수집 |
| Micrometer + Prometheus Registry | 메트릭을 Prometheus 형식으로 노출 |
| Prometheus | 메트릭 수집 및 저장 |
| Grafana | 메트릭 시각화 대시보드 |

---

## 1. 애플리케이션 메트릭 엔드포인트

앱 실행 후 아래 엔드포인트에서 메트릭을 확인할 수 있어요.

| 엔드포인트 | 설명 |
|-----------|------|
| `GET /actuator/health` | 앱/DB/Redis 상태 확인 |
| `GET /actuator/prometheus` | Prometheus 형식 메트릭 전체 조회 |

### 수집되는 주요 메트릭

- **JVM**: 메모리, GC, 스레드
- **HTTP**: 요청 수, 응답 시간, 상태 코드별 통계
- **DB**: HikariCP 커넥션 풀 상태
- **Redis**: Lettuce 커넥션 통계

---

## 2. 로컬 환경 실행

### 사전 준비
```bash
# DB, Redis 먼저 실행
docker compose up -d db redis
```

### 앱 실행
IntelliJ에서 `CarpoolApplication` 실행 (기본 포트: 8080)

### 메트릭 확인
```
http://localhost:8080/actuator/prometheus
http://localhost:8080/actuator/health
```

---

## 3. Prometheus + Grafana 연동 (로컬)

### docker-compose.yml에 추가

```yaml
services:
  prometheus:
    image: prom/prometheus:latest
    container_name: carpool-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    container_name: carpool-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    depends_on:
      - prometheus
```

### prometheus.yml 생성 (프로젝트 루트)

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'carpool'
    static_configs:
      - targets: ['host.docker.internal:8080']
    metrics_path: '/actuator/prometheus'
```

> `host.docker.internal`: Docker 컨테이너에서 호스트 머신의 앱에 접근하기 위한 주소

### 실행

```bash
docker compose up -d prometheus grafana
```

### 접속

| 서비스 | URL | 계정 |
|--------|-----|------|
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin / admin |

### Grafana 대시보드 설정

1. Grafana 접속 → **Connections** → **Add new connection** → Prometheus 선택
2. URL: `http://carpool-prometheus:9090` 입력 후 Save
3. **Dashboards** → **Import** → ID `4701` 입력 (JVM 대시보드)

---

## 4. 배포 환경 (EC2)

EC2에서는 Prometheus가 `http://localhost:8080/actuator/prometheus`를 스크래핑해요.

```yaml
# prometheus.yml (EC2용)
scrape_configs:
  - job_name: 'carpool'
    static_configs:
      - targets: ['carpool-app:8080']
    metrics_path: '/actuator/prometheus'
```

---

## 5. 관련 파일

| 파일 | 변경 내용 |
|------|-----------|
| `backend/build.gradle` | actuator, micrometer-registry-prometheus 의존성 추가 |
| `backend/src/main/resources/application.yml` | health, prometheus 엔드포인트 노출 설정 |
| `backend/src/main/java/.../SecurityConfig.java` | `/actuator/health`, `/actuator/prometheus` 인증 없이 접근 허용 |
