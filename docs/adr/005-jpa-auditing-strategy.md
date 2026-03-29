# ADR-005: JPA Auditing 전략

## 컨텍스트
Contents 테이블의 `created_by`, `created_date`, `last_modified_by`, `last_modified_date` 컬럼을
자동으로 관리해야 한다. 수동으로 설정하면 누락 위험이 있다.

## 결정
**Spring Data JPA Auditing** + **커스텀 AuditorAware**를 사용한다.

## 구현 요약
- `@EnableJpaAuditing`: `JpaAuditingConfiguration`에 설정
- `@CreatedDate`, `@LastModifiedDate`: 시간 자동 기록
- `@CreatedBy`, `@LastModifiedBy`: `AuditorAware<String>` Bean에서 현재 사용자 추출
- `@EntityListeners(AuditingEntityListener.class)`: 엔티티에 리스너 등록

### AuditorAware 구현
```java
@Bean
public AuditorAware<String> auditorProvider() {
    return () -> {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
            || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.of("SYSTEM");
        }
        return Optional.of((String) auth.getPrincipal());
    };
}
```

## 근거
1. 코드 전역에서 `createdBy = currentUser` 같은 수동 설정이 불필요해진다.
2. `SecurityContextHolder`를 통해 JWT에서 추출한 username을 자동으로 사용한다.
3. SQL init 등 미인증 상태에서는 "SYSTEM"을 반환하여 NOT NULL 제약을 충족한다.

## 영향
- Contents 엔티티 생성 시 `createdBy`를 명시적으로 설정할 필요 없음
- 수정 시 `lastModifiedBy`, `lastModifiedDate`가 자동 갱신
