# REST API Documentation

## Base URL
```
http://localhost:8080
```

## 인터랙티브 API 문서
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 인증 방식
JWT Bearer Token — 로그인 후 발급받은 토큰을 `Authorization` 헤더에 포함합니다.
```
Authorization: Bearer {accessToken}
```

## 공통 응답 형식

### 성공 응답
```json
{
  "status": 200,
  "message": "OK",
  "data": { ... }
}
```

### 에러 응답
```json
{
  "status": 400,
  "message": "잘못된 요청입니다.",
  "errors": [
    { "field": "title", "message": "제목은 필수입니다." }
  ]
}
```

---

## 1. 인증 API

### 1-1. 회원가입
- **URL**: `POST /api/members`
- **인증**: 불필요

**Request Body**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| username | String | O | 3~50자 |
| password | String | O | 4~72자 |
| name | String | O | 50자 이하 |

```json
{
  "username": "newuser",
  "password": "password123",
  "name": "홍길동"
}
```

**Response (201 Created)**
```json
{
  "status": 201,
  "message": "Created",
  "data": {
    "id": 3,
    "username": "newuser",
    "name": "홍길동",
    "role": "USER"
  }
}
```

**에러**
| 상태 | 설명 |
|------|------|
| 400 | 유효성 검증 실패 |
| 409 | 이미 존재하는 아이디 |

---

### 1-2. 로그인
- **URL**: `POST /api/auth/login`
- **인증**: 불필요

**Request Body**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| username | String | O | 아이디 |
| password | String | O | 비밀번호 |

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response (200 OK)**
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer"
  }
}
```

**에러**
| 상태 | 설명 |
|------|------|
| 400 | 유효성 검증 실패 |
| 401 | 아이디 또는 비밀번호 불일치 |

---

## 2. 콘텐츠 API

> 모든 콘텐츠 API는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

### 2-1. 콘텐츠 생성
- **URL**: `POST /api/contents`
- **인증**: 필요
- **권한**: 인증된 모든 사용자

**Request Body**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | O | 100자 이하 |
| description | String | X | 콘텐츠 내용 |

```json
{
  "title": "콘텐츠 제목",
  "description": "콘텐츠 내용입니다."
}
```

**Response (201 Created)**
```json
{
  "status": 201,
  "message": "Created",
  "data": {
    "id": 3,
    "title": "콘텐츠 제목",
    "description": "콘텐츠 내용입니다.",
    "viewCount": 0,
    "createdBy": "admin",
    "createdDate": "2026-03-29T14:30:00",
    "lastModifiedBy": "admin",
    "lastModifiedDate": "2026-03-29T14:30:00"
  }
}
```

---

### 2-2. 콘텐츠 목록 조회
- **URL**: `GET /api/contents`
- **인증**: 필요

**Query Parameters**
| 파라미터 | 기본값 | 설명 |
|----------|--------|------|
| page | 0 | 페이지 번호 (0부터) |
| size | 10 | 페이지 크기 |
| sort | createdDate,desc | 정렬 기준 |

**Response (200 OK)**
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

---

### 2-3. 콘텐츠 상세 조회
- **URL**: `GET /api/contents/{id}`
- **인증**: 필요
- **비고**: 조회 시 조회수(viewCount)가 1 증가

**Response (200 OK)**
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "id": 1,
    "title": "첫 번째 콘텐츠",
    "description": "첫 번째 콘텐츠 내용입니다.",
    "viewCount": 1,
    "createdBy": "admin",
    "createdDate": "2026-03-29T14:00:00",
    "lastModifiedBy": null,
    "lastModifiedDate": null
  }
}
```

**에러**
| 상태 | 설명 |
|------|------|
| 404 | 존재하지 않는 콘텐츠 |

---

### 2-4. 콘텐츠 수정
- **URL**: `PUT /api/contents/{id}`
- **인증**: 필요
- **권한**: 콘텐츠 생성자 본인 또는 ADMIN

**Request Body**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | O | 100자 이하 |
| description | String | X | 콘텐츠 내용 |

**에러**
| 상태 | 설명 |
|------|------|
| 400 | 유효성 검증 실패 |
| 403 | 권한 없음 |
| 404 | 존재하지 않는 콘텐츠 |

---

### 2-5. 콘텐츠 삭제
- **URL**: `DELETE /api/contents/{id}`
- **인증**: 필요
- **권한**: 콘텐츠 생성자 본인 또는 ADMIN

**Response (204 No Content)**: 본문 없음

**에러**
| 상태 | 설명 |
|------|------|
| 403 | 권한 없음 |
| 404 | 존재하지 않는 콘텐츠 |

---

## 에러 코드 정리

| HTTP 상태 | 설명 | 발생 상황 |
|-----------|------|-----------|
| 400 | Bad Request | 유효성 검증 실패, 요청 본문 파싱 실패 |
| 401 | Unauthorized | 인증 실패 (토큰 없음/만료/로그인 실패) |
| 403 | Forbidden | 접근 권한 없음 |
| 404 | Not Found | 리소스를 찾을 수 없음 |
| 409 | Conflict | 중복 리소스 |
| 500 | Internal Server Error | 서버 내부 오류 |
