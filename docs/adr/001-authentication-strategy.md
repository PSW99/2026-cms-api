# ADR-001: 인증 방식 선택

## 컨텍스트
CMS REST API에서 Spring Security 기반 로그인을 구현해야 한다.  
로그인 방식은 자유롭게 선택 가능하며, README에 명시해야 한다.

### 후보군
| 방식 | 장점 | 단점 |
|------|------|------|
| Session + Cookie | 구현 단순, Spring Security 기본 지원 | 서버 상태 유지 필요, REST 철학과 불일치 |
| JWT (Access Token) | Stateless, REST API에 적합, 확장성 | 토큰 탈취 시 만료까지 무효화 어려움 |
| JWT (Access + Refresh) | 보안 강화, Access Token 짧은 만료 | 구현 복잡도 증가, 과제 범위 초과 가능 |

## 결정
**JWT Access Token 단일 방식**을 채택한다.

## 근거
1. **REST API 특성과의 일치**: Stateless 통신이 REST 원칙에 부합한다.
2. **과제 범위 적합성**: Access Token만으로 인증/인가를 충분히 시연할 수 있다.
3. **클라이언트 편의성**: Authorization 헤더에 Bearer 토큰을 담아 요청하는 방식은 API 테스트와 문서화에 직관적이다.
4. **Spring Security 통합**: `OncePerRequestFilter`를 확장하여 JWT 검증 필터를 Security Filter Chain에 자연스럽게 통합할 수 있다.

## 구현 요약
- **토큰 생성**: 로그인 성공 시 JWT Access Token 발급 (만료: 1시간)
- **토큰 구조**: Claims에 username(subject), role 정보 포함
- **인증 필터**: `JwtAuthenticationFilter` → `OncePerRequestFilter` 확장
- **비밀번호 암호화**: `BCryptPasswordEncoder`
- **JWT 라이브러리**: jjwt 0.12.6 (`parseSignedClaims` API)

## 영향
- 회원가입(`POST /api/members`)과 로그인(`POST /api/auth/login`)은 인증 없이 접근 가능
- 나머지 API는 유효한 JWT 토큰 필요
- H2 Console, Actuator, Swagger UI는 별도 SecurityFilterChain으로 허용
