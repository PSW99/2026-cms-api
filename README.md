# CMS REST API

간단한 CMS(Contents Management System) REST API입니다.  
JWT 기반 Stateless 인증, 콘텐츠 CRUD, 역할 기반 접근 권한을 구현했습니다.

## 기술 스택

- **Java 25** (Amazon Corretto)
- **Spring Boot 4.0.3**
- **Spring Security** — JWT 기반 Stateless 인증
- **Spring Data JPA** — ORM + Auditing (`@CreatedBy`, `@LastModifiedBy`)
- **H2 Database** — 인메모리 DB
- **QueryDSL 7.1** — 타입 안전 쿼리 (확장 대비)
- **jjwt 0.12.6** — JWT 토큰 생성/검증
- **springdoc-openapi 3.0.1** — Swagger UI / OpenAPI 3 자동 생성
- **Lombok**, **p6spy** (SQL 로깅)

## 인증 방식

**JWT (JSON Web Token) Access Token** 방식을 사용합니다.

- 로그인 성공 시 JWT Access Token을 발급합니다 (유효 시간: 1시간)
- 이후 요청 시 `Authorization: Bearer {token}` 헤더에 토큰을 포함합니다
- 서버는 Stateless로 동작하며, 세션을 사용하지 않습니다
- 선택 근거는 `docs/adr/001-authentication-strategy.md`에 기록했습니다

## 실행 방법

### 사전 조건
- Java 25 설치 (sdkman 사용 시: `sdk install java 25.0.1-amzn`)
- `src/main/resources/application-local.yml`에 JWT secret 설정 필요:

```yaml
jwt:
  secret: dGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIGp3dCB0b2tlbiBnZW5lcmF0aW9uIDIwMjY=
```

### 실행
```bash
./gradlew bootRun
```

### 접속
- **API 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:test`
  - Username: `sa` / Password: (없음)

### 초기 계정
| 아이디 | 비밀번호 | 역할 |
|--------|----------|------|
| admin | admin123 | ADMIN |
| user1 | user123 | USER |

## API 사용 예시

### 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 콘텐츠 생성
```bash
curl -X POST http://localhost:8080/api/contents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {토큰}" \
  -d '{"title": "새 콘텐츠", "description": "내용입니다."}'
```

### 콘텐츠 목록 조회 (페이징)
```bash
curl "http://localhost:8080/api/contents?page=0&size=10" \
  -H "Authorization: Bearer {토큰}"
```

## API 엔드포인트

| Method | URL | 설명 | 인증 | 권한 |
|--------|-----|------|------|------|
| POST | `/api/members` | 회원가입 | X | - |
| POST | `/api/auth/login` | 로그인 | X | - |
| POST | `/api/contents` | 콘텐츠 생성 | O | 모든 사용자 |
| GET | `/api/contents` | 목록 조회 (페이징) | O | 모든 사용자 |
| GET | `/api/contents/{id}` | 상세 조회 (조회수 증가) | O | 모든 사용자 |
| PUT | `/api/contents/{id}` | 수정 | O | 본인 / ADMIN |
| DELETE | `/api/contents/{id}` | 삭제 | O | 본인 / ADMIN |

상세 API 명세는 `docs/REST-API-DOCS.md` 또는 Swagger UI를 참고해주세요.

## 접근 권한 정책

- **콘텐츠 조회** (목록/상세): 인증된 모든 사용자
- **콘텐츠 생성**: 인증된 모든 사용자
- **콘텐츠 수정/삭제**: 콘텐츠 생성자 본인 또는 ADMIN
- **회원가입, 로그인**: 인증 불필요

## 프로젝트 구조

```
src/main/java/com/malgn/
├── Application.java
├── auth/                          # 인증
│   ├── controller/AuthController
│   ├── dto/LoginRequest, LoginResponse
│   ├── filter/JwtAuthenticationFilter
│   └── provider/JwtTokenProvider
├── member/                        # 회원
│   ├── controller/MemberController
│   ├── dto/MemberCreateRequest, MemberResponse
│   ├── entity/Member, Role
│   ├── repository/MemberRepository
│   └── service/MemberService
├── contents/                      # 콘텐츠
│   ├── controller/ContentsController
│   ├── dto/ContentsCreateRequest, ContentsUpdateRequest, ContentsResponse
│   ├── entity/Contents
│   ├── repository/ContentsRepository
│   └── service/ContentsService
├── common/                        # 공통
│   ├── config/JpaAuditingConfiguration
│   ├── dto/ApiResponse, PageResponse
│   └── exception/GlobalExceptionHandler, EntityNotFoundException, DuplicateResourceException
└── configure/                     # 설정
    ├── OpenApiConfiguration
    ├── AppConfiguration
    └── security/
        ├── SecurityConfiguration
        ├── ActuatorSecurityConfiguration
        ├── H2DbSecurityConfiguration
        ├── JwtAuthenticationEntryPoint
        └── JwtAccessDeniedHandler
```

## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

### 테스트 구성
| 테스트 클래스 | 유형 | 개수 |
|--------------|------|------|
| JwtTokenProviderTest | 단위 | 11 |
| MemberServiceTest | 단위 (Mockito) | 5 |
| ContentsServiceTest | 단위 (Mockito) | 15 |
| AuthControllerTest | 통합 (MockMvc) | 6 |
| MemberControllerTest | 통합 (MockMvc) | 8 |
| ContentsControllerTest | 통합 (MockMvc) | 20 |

## 설계 결정 (ADR)

주요 설계 결정은 `docs/adr/` 디렉토리에 기록했습니다.

- `001-authentication-strategy.md` — 인증 방식 선택 (JWT)
- `002-api-design-convention.md` — REST API 설계 규칙
- `003-exception-handling-strategy.md` — 예외 처리 전략
- `004-authorization-strategy.md` — 콘텐츠 접근 권한 전략
- `005-jpa-auditing-strategy.md` — JPA Auditing 전략

## CI/CD

GitHub Actions로 push/PR 시 자동 빌드 + 테스트를 수행합니다.
`.github/workflows/ci.yml` 참고.

## 사용 도구

- **Claude (Anthropic)** — 코드 구현 보조, 설계 결정 논의, 문서 작성 보조
- **Coderabbit** - PR 코드 리뷰
