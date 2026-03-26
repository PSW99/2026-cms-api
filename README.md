# 2026 신입 Back-End 개발자 코딩 과제 - 간단한 CMS REST API

2026년도 신입 Back-End 개발자 코딩 과제입니다.
간단한 CMS(Contents Management System) REST API 를 구현하는 것이 목표입니다.

외부 자료 검색 및 AI 도구 사용을 허용합니다. 다만, 제출물에 활용한 도구와 방식을 간단하게 명시해주시기 바랍니다.

## Spec

- Java 25
- Spring Boot 4
- Spring Security
- JPA
- H2 (db)
- Lombok (필요시)

## 과제 목표

- 간단한 CMS 콘텐츠 관리 API 를 구현 해주세요.
- DB Schema 모두 구현해주세요.
- DB 는 h2 를 사용해주세요.
- 가능한 예외처리도 구현해주세요.
- 필요하다고 생각되는 부분은 추가로 구현해도 됩니다.

## 데이터 모델

### Contents

| 컬럼명                | 이름  | 설명          | 데이터 타입                      | 비고 |
|--------------------|-----|-------------|-----------------------------|----|
| id                 | 아이디 | 고유 아이디      | bigint primary key not null |    |
| title              | 제목  | contents 제목 | varchar(100) not null       |    |
| description        | 내용  | contents 내용 | text                        |    |
| view_count         | 조회수 | 조회수         | bigint not null             |    |
| created_date       | 생성일 | 생성한 날짜      | timestamp                   |    |
| created_by         | 생성자 | 생성한 사용자     | varchar(50) not null        |    |
| last_modified_date | 수정일 | 마지막 수정일     | timestamp                   |    |
| last_modified_by   | 수정자 | 마지막 수정한 사용자 | varchar(50)                 |    |

## 구현 기능

### 콘텐츠 관련 CRUD

시스템에 등록된 콘텐츠에 대한 CRUD 를 필수로 구현해주세요.

#### 기능
- 콘텐츠 추가
- 콘텐츠 목록 조회
  - 반드시 페이징 처리를 해주세요.
- 콘텐츠 상세 조회
- 콘텐츠 수정
- 콘텐츠 삭제


### 로그인
- Spring Security 를 이용해서 로그인을 필수로 구현해주세요.
- 로그인 방식은 자유롭게 선택하여 구현하되, `README.md` 에 명시해주세요
- Role
    - 관리자(ADMIN)
    - 사용자(USER)

### 접근 권한

- 접근 권한을 필수로 구현해주세요.
- 콘텐츠 생성자 본인만 수정 + 삭제 가능하게 구현해주세요.
- 단, 관리자(ADMIN) 인 경우 모든 콘텐츠에 대해 수정 + 삭제할 수 있게 구현해주세요.

## 제출

### 기한

- 본 메일 수신 후 26.03.09(월) 오후 3시까지 (주)맑은기술 채용 메일(recruit@malgn.com) 로 보내주시기 바랍니다. 

### 제출물

- 소스코드 (Zip 또는 Github repository 링크)
- README.md
    - 추가 내용이나 제출물 관련 내용을 추가헤주세요.
    - 사용한 AI 또는 참고 자료가 있다면 간단히 명시
- REST API Docs
    - 자유롭게 작성해서 첨부해주세요.










