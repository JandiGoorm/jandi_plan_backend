# 프로젝트 개선 로드맵

**작성일**: 2026-01-02

본 문서는 `jandi_plan_backend` 프로젝트의 안정성, 성능, 유지보수성 향상을 위한 단계별 개선 계획을 기술함.

## 로드맵 개요

| 우선순위 | 작업명 | 카테고리 | 난이도 | 목적 |
| :--- | :--- | :--- | :--- | :--- |
| **P1** | **무중단 배포(Blue-Green) 도입** | Infra | 상 | 배포 시 서비스 다운타임 제거 |
| **P2** | **Controller 테스트 구축** | Testing | 중 | API 변경 감지 및 안정성 확보 |
| **P3** | **운영 DB 설정 변경(ddl-auto)** | DB | 상 | 데이터 유실 방지(Human Error) |
| **P4** | **외부 API 연동 개선(Resilience)** | Code | 중 | 스레드 고갈 방지 및 장애 격리 |
| **P5** | **ValidationUtil 의존성 분리** | Refactoring | 상 | 순환 참조 방지 및 결합도 감소 |
| **P6** | **API 문서 자동화 도입** | Doc | 중 | 협업 효율 증대 |

---

## P1: 무중단 배포 (Blue-Green Deployment)

### 현상 (As-Is)
- `deploy.sh` 실행 시 `docker-compose down` 후 `up` 수행.
- 컨테이너 재생성 시간(10~30초) 동안 502 Bad Gateway 발생.

### 목표 (To-Be)
- Blue-Green 전략 적용.
- 신규 버전(Green) 기동 및 검증 후 트래픽 전환으로 다운타임 제거.

### 실행 계획
1.  **Nginx 도입**: 리버스 프록시 구성.
2.  **Dual Target 구성**: Blue(8081) / Green(8082) 포트 분리.
3.  **배포 스크립트 개선**:
    - 가용 포트 확인 및 컨테이너 실행.
    - Health Check(`curl /actuator/health`) 수행.
    - Nginx 트래픽 전환(`reload`) 및 구 버전 종료.

---

## P2: Controller 테스트 (Testing)

### 현상 (As-Is)
- Service 레이어 테스트는 존재하나 Controller 테스트 부재.
- API 스펙(URL, 파라미터, 응답값) 변경 시 검증 불가.

### 목표 (To-Be)
- 모든 Controller 대상 `@WebMvcTest` 작성.
- MockMvc 활용 HTTP 요청/응답 검증.

### 실행 계획
1.  **BaseControllerTest 작성**: 공통 설정(MockMvc, Jackson 등) 분리.
2.  **기능별 테스트 구현**: 조회/생성/수정/삭제 시나리오별 성공/실패 케이스 작성.
3.  **CI 연동**: Jenkins 빌드 파이프라인 수행 확인.



---

## P3: 운영 DB 설정 변경 (Database)

### 현상 (As-Is)
- `application.properties`에 `spring.jpa.hibernate.ddl-auto=update` 설정.
- 배포 시 실수로 엔티티가 변경되면 운영 DB 스키마가 의도치 않게 변경될 위험 존재.

### 목표 (To-Be)
- 운영 환경(Prod)에서는 `validate` 또는 `none` 사용.

### 실행 계획
1.  **프로파일 분리**: `application-prod.properties` 생성.
2.  **설정 변경**: `ddl-auto=validate` 적용.

---

## P4: 외부 API 연동 개선 (Resilience)

### 현상 (As-Is)
- `RecommendService`에서 Google Maps API 호출 시 `Thread.sleep(2000)` 사용.
- 동기(Blocking) 호출 방식(`await()`)으로 인해 외부 서비스 지연 시 톰캣 스레드 풀 고갈 위험.

### 목표 (To-Be)
- Circuit Breaker(Resilience4j) 도입.
- 비동기 처리 또는 적절한 타임아웃 적용.

### 실행 계획
1.  **Resilience4j 의존성 추가**.
2.  **Circuit Breaker 설정**: 실패율 임계치 설정.
3.  **Refactoring**: `Thread.sleep` 제거 및 재시도 로직 개선.

---

## P5: ValidationUtil 의존성 분리 (Refactoring)

### 현상 (As-Is)
- `ValidationUtil`이 11개의 Repository를 주입받아 사용.
- 사실상 Service 역할을 수행하며 높은 결합도(Coupling) 발생.

### 목표 (To-Be)
- 검증 로직을 각 도메인 Service 또는 Entity 내부로 이동.

### 실행 계획
1.  **책임 분산**: `validateUserExists` -> `UserService`로 이동.
2.  **Util 축소**: 순수 유틸리티 성격의 메서드만 유지.

---

## P6: API 문서 자동화 (Documentation)

### 현상 (As-Is)
- API 문서 부재 또는 수동 관리.
- 변경 사항 추적 어려움.

### 목표 (To-Be)
- 코드 기반 문서 자동화(Swagger/RestDocs) 구축.

### 실행 계획
1.  **Swagger(Springdoc) 설정**.
2.  **Controller 테스트(P2)** 완료 후 적용 권장.
