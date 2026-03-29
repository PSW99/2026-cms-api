# ADR-004: 콘텐츠 접근 권한 체크 전략

## 컨텍스트
콘텐츠 수정/삭제는 **생성자 본인** 또는 **ADMIN** 만 가능해야 한다.  
이 권한 체크를 어디서, 어떻게 수행할지 결정해야 한다.

### 후보군
| 방식 | 장점 | 단점 |
|------|------|------|
| Service 레이어에서 직접 검증 | 직관적, 디버깅 용이 | 비즈니스 로직과 권한 로직 혼재 |
| `@PreAuthorize` + SpEL | 선언적, 관심사 분리 | SpEL 표현식 복잡, 테스트 어려움 |
| 커스텀 어노테이션 + AOP | 깔끔한 분리, 재사용 | 과제 규모 대비 오버엔지니어링 |

## 결정
**Service 레이어에서 직접 검증** 방식을 채택한다.

## 근거
1. 코드를 읽을 때 권한 체크 로직이 바로 보여 코드 리뷰어가 흐름을 빠르게 파악할 수 있다.
2. 콘텐츠 엔티티 하나에 대한 권한 체크이므로 AOP나 SpEL을 도입할 만큼 복잡하지 않다.
3. 서비스 단위 테스트에서 권한 체크 로직을 직접 검증할 수 있다.

## 구현 요약
```java
private void validateOwnerOrAdmin(Contents contents) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = (String) auth.getPrincipal();

    boolean isAdmin = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_ADMIN"::equals);

    if (isAdmin) return;  // ADMIN은 모든 콘텐츠 수정/삭제 가능

    if (!contents.getCreatedBy().equals(currentUsername)) {
        throw new AccessDeniedException("본인이 작성한 콘텐츠만 수정/삭제할 수 있습니다.");
    }
}
```

- ADMIN 체크를 먼저 수행하여 불필요한 문자열 비교 방지
- `SecurityContextHolder`에서 현재 사용자 정보 추출
- JWT 토큰의 `principal`이 username(String)이므로 캐스팅 가능

## 영향
- `update()`, `delete()` 메서드에서 `validateOwnerOrAdmin()` 호출
- 권한 없는 접근 시 `AccessDeniedException` → `GlobalExceptionHandler`에서 403 응답
