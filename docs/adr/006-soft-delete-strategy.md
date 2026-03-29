# ADR-006: 소프트 삭제 전략

## 컨텍스트
콘텐츠 삭제 시 데이터를 물리적으로 제거하면 복구가 불가능하다.
관리자가 실수로 삭제된 콘텐츠를 복원하거나 삭제 이력을 추적할 수 있어야 한다.

### 후보군
| 방식 | 장점 | 단점 |
|------|------|------|
| Hard Delete | 단순, 스토리지 절약 | 복구 불가, 이력 추적 불가 |
| Soft Delete (boolean 플래그) | 복구 가능, 구현 단순 | 모든 조회 쿼리에 필터 조건 필요 |
| Soft Delete + `@SQLRestriction` | 자동 필터링으로 누락 방지 | Hibernate 의존, 삭제된 데이터 조회 시 우회 필요 |
| 이력 테이블 분리 | 깔끔한 분리 | 테이블 2개 관리, 과제 규모 대비 과도 |

## 결정
**Soft Delete (boolean 플래그)** + **Repository 쿼리 오버라이드** 방식을 채택한다.

## 근거
1. `@SQLRestriction`은 자동 필터링이 편리하지만, 삭제된 콘텐츠를 조회하려면 네이티브 쿼리 우회가 필요해 오히려 복잡해진다.
2. Repository에서 `findById`/`findAll`을 `deleted = false` 조건으로 오버라이드하면 일반 조회에서 삭제 항목이 확실히 제외된다.
3. 삭제된 항목 조회는 `findByIdIncludingDeleted`, `findAllDeleted` 같은 명시적 메서드로 분리하여 의도를 드러낸다.
4. 과제 규모에서 이력 테이블 분리는 불필요한 복잡도를 추가한다.

## 구현 요약

### 엔티티
```java
@Column(nullable = false)
private Boolean deleted;       // 삭제 여부

@Column(name = "deleted_date")
private LocalDateTime deletedDate; // 삭제 시각

public void softDelete() {
    this.deleted = true;
    this.deletedDate = LocalDateTime.now();
}

public void restore() {
    this.deleted = false;
    this.deletedDate = null;
}
```

### Repository
```java
@Query("SELECT c FROM Contents c WHERE c.id = :id AND c.deleted = false")
Optional<Contents> findById(@Param("id") Long id);

@Query("SELECT c FROM Contents c WHERE c.deleted = false")
Page<Contents> findAll(Pageable pageable);

@Query("SELECT c FROM Contents c WHERE c.id = :id")
Optional<Contents> findByIdIncludingDeleted(@Param("id") Long id);

@Query("SELECT c FROM Contents c WHERE c.deleted = true")
Page<Contents> findAllDeleted(Pageable pageable);
```

### API
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| DELETE | `/api/contents/{id}` | 소프트 삭제 | 본인 / ADMIN |
| GET | `/api/contents/deleted` | 삭제된 목록 조회 | ADMIN |
| PATCH | `/api/contents/{id}/restore` | 복원 | ADMIN |

## 영향
- `DELETE` 요청 시 `contentsRepository.delete()` 대신 `contents.softDelete()` 호출
- 일반 목록/상세 조회에서 `deleted = true` 항목이 자동 제외
- ADMIN만 삭제된 콘텐츠 조회 및 복원 가능
