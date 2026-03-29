# ADR-002: REST API 설계 규칙

## 컨텍스트
CMS REST API의 URL 구조, HTTP 메서드, 요청/응답 포맷에 대한 일관된 규칙이 필요하다.

## 결정

### URL 규칙
- Base path: `/api`
- 리소스명은 복수형 명사 사용
- 케밥 케이스 사용

### 엔드포인트 설계
| 기능 | Method | URL | 인증 |
|------|--------|-----|------|
| 회원가입 | POST | `/api/members` | X |
| 로그인 | POST | `/api/auth/login` | X |
| 콘텐츠 생성 | POST | `/api/contents` | O |
| 콘텐츠 목록 조회 | GET | `/api/contents` | O |
| 콘텐츠 상세 조회 | GET | `/api/contents/{id}` | O |
| 콘텐츠 수정 | PUT | `/api/contents/{id}` | O |
| 콘텐츠 삭제 | DELETE | `/api/contents/{id}` | O |
| 삭제된 콘텐츠 목록 | GET | `/api/contents/deleted` | O (ADMIN) |
| 삭제된 콘텐츠 복원 | PATCH | `/api/contents/{id}/restore` | O (ADMIN) |

### 공통 응답 포맷
```json
{
  "status": 200,
  "message": "OK",
  "data": { ... }
}
```

### 에러 응답 포맷
```json
{
  "status": 400,
  "message": "잘못된 요청입니다.",
  "errors": [
    { "field": "title", "message": "제목은 필수입니다." }
  ]
}
```

### 페이징 응답
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5
  }
}
```

## 근거
1. 모든 응답을 동일한 구조로 래핑하면 클라이언트가 예측 가능하게 처리할 수 있다.
2. 복수형 명사, 적절한 HTTP 메서드 매핑은 업계 표준이다.
3. Spring Data의 `Pageable`을 활용하되, `PageResponse`로 불필요한 메타데이터를 제거한다.
4. `@JsonInclude(NON_NULL)`로 null 필드를 응답에서 제외하여 깔끔한 JSON을 제공한다.
