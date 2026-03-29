# ADR-003: 예외 처리 전략

## 컨텍스트
REST API에서 발생하는 다양한 예외를 일관된 형태로 클라이언트에 전달해야 한다.

### 후보군
| 방식 | 장점 | 단점 |
|------|------|------|
| Controller별 `@ExceptionHandler` | 세밀한 제어 가능 | 중복 코드, 일관성 유지 어려움 |
| `@RestControllerAdvice` 글로벌 핸들러 | 중앙 관리, 일관된 응답 | 특정 컨트롤러만의 예외 처리 불편 |

## 결정
**`@RestControllerAdvice` 기반 글로벌 예외 핸들러**를 채택한다.

## 근거
1. 모든 예외가 동일한 `ApiResponse` 포맷으로 반환된다.
2. 예외 처리 로직이 한 곳에 모여 유지보수가 용이하다.
3. 과제 규모에서 컨트롤러가 많지 않아 글로벌 핸들러 하나로 충분하다.

## 구현된 예외 매핑

| 예외 | HTTP 상태 | 설명 |
|------|----------|------|
| `EntityNotFoundException` | 404 | 리소스를 찾을 수 없음 |
| `AccessDeniedException` | 403 | 권한 없음 (본인 콘텐츠가 아닌 경우) |
| `BadCredentialsException` | 401 | 로그인 실패 (아이디/비밀번호 불일치) |
| `MethodArgumentNotValidException` | 400 | 요청 DTO 유효성 검증 실패 |
| `HttpMessageNotReadableException` | 400 | 요청 본문 파싱 실패 (빈 body 등) |
| `DuplicateResourceException` | 409 | 중복 리소스 (동일 username) |
| `IllegalArgumentException` | 400 | 잘못된 인자 |
| `Exception` | 500 | 예상치 못한 서버 에러 |

## Security 예외 처리
Spring Security의 `AuthenticationException`과 `AccessDeniedException`은 Controller 밖에서 발생하므로 `@RestControllerAdvice`가 잡지 못한다. 이를 위해 별도 핸들러를 구현했다.

- `JwtAuthenticationEntryPoint`: 인증 실패 → 401 JSON 응답
- `JwtAccessDeniedHandler`: 접근 거부 → 403 JSON 응답
- 두 핸들러 모두 `ObjectMapper`로 `ApiResponse` 포맷의 JSON을 직접 작성

## 영향
- 500 에러의 상세 내용은 서버 로그에만 남기고, 클라이언트에는 일반적인 메시지 반환
- `MethodArgumentNotValidException` 처리 시 필드별 에러 목록을 `ApiResponse.FieldError`로 변환
