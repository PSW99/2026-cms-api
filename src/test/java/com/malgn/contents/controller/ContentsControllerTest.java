package com.malgn.contents.controller;

import com.malgn.auth.provider.JwtTokenProvider;
import com.malgn.contents.dto.ContentsCreateRequest;
import com.malgn.contents.dto.ContentsUpdateRequest;
import com.malgn.contents.entity.Contents;
import com.malgn.contents.repository.ContentsRepository;
import com.malgn.member.entity.Member;
import com.malgn.member.entity.Role;
import com.malgn.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
@Transactional
class ContentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ContentsRepository contentsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private String adminToken;
    private String user1Token;
    private String user2Token;

    private Long contentsId1;
    private Long contentsId2;

    @BeforeEach
    void setUp() {
        // 회원 생성
        memberRepository.save(Member.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .name("관리자")
                .role(Role.ADMIN)
                .build());
        memberRepository.save(Member.builder()
                .username("user1")
                .password(passwordEncoder.encode("user123"))
                .name("사용자1")
                .role(Role.USER)
                .build());

        // admin으로 SecurityContext 설정 후 콘텐츠 1 생성
        setSecurityContext("admin", "ROLE_ADMIN");
        Contents c1 = contentsRepository.save(Contents.builder()
                .title("첫 번째 콘텐츠")
                .description("관리자가 작성한 콘텐츠")
                .build());
        contentsId1 = c1.getId();

        // user1로 SecurityContext 설정 후 콘텐츠 2 생성
        setSecurityContext("user1", "ROLE_USER");
        Contents c2 = contentsRepository.save(Contents.builder()
                .title("두 번째 콘텐츠")
                .description("사용자1이 작성한 콘텐츠")
                .build());
        contentsId2 = c2.getId();

        // DB에 반영 후 SecurityContext 정리
        entityManager.flush();
        SecurityContextHolder.clearContext();

        // JWT 토큰 생성
        adminToken = jwtTokenProvider.generateToken("admin", "ADMIN");
        user1Token = jwtTokenProvider.generateToken("user1", "USER");
        user2Token = jwtTokenProvider.generateToken("user2", "USER");
    }

    private void setSecurityContext(String username, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Nested
    @DisplayName("인증 검증")
    class Authentication {

        @Test
        @DisplayName("토큰 없이 콘텐츠 API 접근 시 401을 반환한다")
        void noTokenReturns401() throws Exception {
            mockMvc.perform(get("/api/contents"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
        }

        @Test
        @DisplayName("잘못된 토큰으로 접근 시 401을 반환한다")
        void invalidTokenReturns401() throws Exception {
            mockMvc.perform(get("/api/contents")
                            .header("Authorization", "Bearer invalid.token.here"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/contents - 콘텐츠 생성")
    class Create {

        @Test
        @DisplayName("인증된 사용자가 콘텐츠를 생성하면 201을 반환한다")
        void createSuccess() throws Exception {
            // given
            ContentsCreateRequest request = new ContentsCreateRequest("새 콘텐츠", "내용입니다.");

            // when & then
            mockMvc.perform(post("/api/contents")
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.title").value("새 콘텐츠"))
                    .andExpect(jsonPath("$.data.description").value("내용입니다."))
                    .andExpect(jsonPath("$.data.viewCount").value(0))
                    .andExpect(jsonPath("$.data.createdBy").value("admin"));
        }

        @Test
        @DisplayName("description 없이 생성할 수 있다")
        void createWithoutDescription() throws Exception {
            // given
            ContentsCreateRequest request = new ContentsCreateRequest("제목만", null);

            // when & then
            mockMvc.perform(post("/api/contents")
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.title").value("제목만"))
                    .andExpect(jsonPath("$.data.description").doesNotExist());
        }

        @Test
        @DisplayName("제목이 빈값이면 400을 반환한다")
        void blankTitle() throws Exception {
            // given
            ContentsCreateRequest request = new ContentsCreateRequest("", "내용");

            // when & then
            mockMvc.perform(post("/api/contents")
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[?(@.field == 'title')]").exists());
        }

        @Test
        @DisplayName("제목이 100자를 초과하면 400을 반환한다")
        void titleTooLong() throws Exception {
            // given
            String longTitle = "a".repeat(101);
            ContentsCreateRequest request = new ContentsCreateRequest(longTitle, "내용");

            // when & then
            mockMvc.perform(post("/api/contents")
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[?(@.field == 'title')]").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/contents - 콘텐츠 목록 조회")
    class GetList {

        @Test
        @DisplayName("페이징된 목록을 반환한다")
        void getListSuccess() throws Exception {
            // h2-data.sql에 2건의 샘플 데이터가 있음
            mockMvc.perform(get("/api/contents")
                            .header("Authorization", bearer(adminToken))
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.totalElements").isNumber())
                    .andExpect(jsonPath("$.data.totalPages").isNumber());
        }

        @Test
        @DisplayName("size=1로 요청하면 1건만 반환한다")
        void getListWithSmallPageSize() throws Exception {
            mockMvc.perform(get("/api/contents")
                            .header("Authorization", bearer(adminToken))
                            .param("page", "0")
                            .param("size", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.size").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/contents/{id} - 콘텐츠 상세 조회")
    class GetDetail {

        @Test
        @DisplayName("상세 조회 시 조회수가 증가한다")
        void viewCountIncreases() throws Exception {
            mockMvc.perform(get("/api/contents/" + contentsId1)
                            .header("Authorization", bearer(adminToken)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.viewCount").value(1));
        }

        @Test
        @DisplayName("두 번 조회하면 조회수가 2가 된다")
        void viewCountAccumulates() throws Exception {
            // when - 두 번 조회
            mockMvc.perform(get("/api/contents/" + contentsId1)
                    .header("Authorization", bearer(adminToken)));

            // then - 두 번째 조회 결과 확인
            mockMvc.perform(get("/api/contents/" + contentsId1)
                            .header("Authorization", bearer(adminToken)))
                    .andDo(print())
                    .andExpect(jsonPath("$.data.viewCount").value(2));
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 조회 시 404를 반환한다")
        void notFound() throws Exception {
            mockMvc.perform(get("/api/contents/999")
                            .header("Authorization", bearer(adminToken)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("콘텐츠를 찾을 수 없습니다. id=999"));
        }
    }

    @Nested
    @DisplayName("PUT /api/contents/{id} - 콘텐츠 수정")
    class Update {

        @Test
        @DisplayName("본인 콘텐츠를 수정할 수 있다")
        void ownerCanUpdate() throws Exception {
            ContentsUpdateRequest request = new ContentsUpdateRequest("수정 제목", "수정 내용");

            mockMvc.perform(put("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(user1Token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정 제목"))
                    .andExpect(jsonPath("$.data.description").value("수정 내용"));
        }

        @Test
        @DisplayName("ADMIN은 다른 사용자의 콘텐츠를 수정할 수 있다")
        void adminCanUpdateOthers() throws Exception {
            ContentsUpdateRequest request = new ContentsUpdateRequest("관리자 수정", "관리자 내용");

            mockMvc.perform(put("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("관리자 수정"));
        }

        @Test
        @DisplayName("다른 사용자의 콘텐츠를 수정하면 403을 반환한다")
        void otherUserCannotUpdate() throws Exception {
            ContentsUpdateRequest request = new ContentsUpdateRequest("해킹", "시도");

            mockMvc.perform(put("/api/contents/" + contentsId1)
                            .header("Authorization", bearer(user1Token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠를 수정하면 404를 반환한다")
        void updateNotFound() throws Exception {
            ContentsUpdateRequest request = new ContentsUpdateRequest("수정", "내용");

            mockMvc.perform(put("/api/contents/999")
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("제목이 빈값이면 400을 반환한다")
        void blankTitle() throws Exception {
            ContentsUpdateRequest request = new ContentsUpdateRequest("", "내용");

            mockMvc.perform(put("/api/contents/" + contentsId1)
                            .header("Authorization", bearer(adminToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/contents/{id} - 콘텐츠 삭제")
    class Delete {

        @Test
        @DisplayName("본인 콘텐츠를 삭제할 수 있다")
        void ownerCanDelete() throws Exception {
            mockMvc.perform(delete("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(user1Token)))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 삭제 후 조회 시 404
            mockMvc.perform(get("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(user1Token)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("ADMIN은 다른 사용자의 콘텐츠를 삭제할 수 있다")
        void adminCanDeleteOthers() throws Exception {
            mockMvc.perform(delete("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(adminToken)))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("다른 사용자의 콘텐츠를 삭제하면 403을 반환한다")
        void otherUserCannotDelete() throws Exception {
            mockMvc.perform(delete("/api/contents/" + contentsId1)
                            .header("Authorization", bearer(user1Token)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠를 삭제하면 404를 반환한다")
        void deleteNotFound() throws Exception {
            mockMvc.perform(delete("/api/contents/999")
                            .header("Authorization", bearer(adminToken)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("소프트 삭제 및 복원")
    class SoftDelete {

        @Test
        @DisplayName("삭제된 콘텐츠는 목록에서 조회되지 않는다")
        void deletedContentNotInList() throws Exception {
            // given - 콘텐츠 삭제
            mockMvc.perform(delete("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(user1Token)))
                    .andExpect(status().isNoContent());

            // when & then - 목록 조회 시 삭제된 항목 제외
            mockMvc.perform(get("/api/contents")
                            .header("Authorization", bearer(adminToken))
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("삭제된 콘텐츠는 상세 조회 시 404를 반환한다")
        void deletedContentReturns404() throws Exception {
            // given
            mockMvc.perform(delete("/api/contents/" + contentsId1)
                            .header("Authorization", bearer(adminToken)))
                    .andExpect(status().isNoContent());

            // when & then
            mockMvc.perform(get("/api/contents/" + contentsId1)
                            .header("Authorization", bearer(adminToken)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("ADMIN이 삭제된 콘텐츠 목록을 조회할 수 있다")
        void adminCanGetDeletedList() throws Exception {
            // given - 콘텐츠 삭제
            mockMvc.perform(delete("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(user1Token)))
                    .andExpect(status().isNoContent());

            // when & then
            mockMvc.perform(get("/api/contents/deleted")
                            .header("Authorization", bearer(adminToken))
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].deleted").value(true));
        }

        @Test
        @DisplayName("일반 사용자는 삭제된 콘텐츠 목록을 조회할 수 없다")
        void userCannotGetDeletedList() throws Exception {
            mockMvc.perform(get("/api/contents/deleted")
                            .header("Authorization", bearer(user1Token)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN이 삭제된 콘텐츠를 복원할 수 있다")
        void adminCanRestore() throws Exception {
            // given - 콘텐츠 삭제
            mockMvc.perform(delete("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(adminToken)))
                    .andExpect(status().isNoContent());

            // when - 복원
            mockMvc.perform(patch("/api/contents/" + contentsId2 + "/restore")
                            .header("Authorization", bearer(adminToken)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.deleted").value(false))
                    .andExpect(jsonPath("$.data.title").value("두 번째 콘텐츠"));

            // then - 다시 조회 가능
            mockMvc.perform(get("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(adminToken)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("일반 사용자는 콘텐츠를 복원할 수 없다")
        void userCannotRestore() throws Exception {
            // given
            mockMvc.perform(delete("/api/contents/" + contentsId2)
                            .header("Authorization", bearer(user1Token)))
                    .andExpect(status().isNoContent());

            // when & then
            mockMvc.perform(patch("/api/contents/" + contentsId2 + "/restore")
                            .header("Authorization", bearer(user1Token)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("삭제되지 않은 콘텐츠를 복원하면 400을 반환한다")
        void restoreNotDeletedReturns400() throws Exception {
            mockMvc.perform(patch("/api/contents/" + contentsId1 + "/restore")
                            .header("Authorization", bearer(adminToken)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
