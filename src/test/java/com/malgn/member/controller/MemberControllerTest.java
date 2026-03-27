package com.malgn.member.controller;

import tools.jackson.databind.ObjectMapper;
import com.malgn.member.dto.MemberCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/members")
    class CreateMember {

        @Test
        @DisplayName("정상적으로 회원을 생성하면 201을 반환한다")
        void createSuccess() throws Exception {
            // given
            MemberCreateRequest request = new MemberCreateRequest("newuser", "password123", "홍길동");

            // when & then
            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Created"))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.role").value("USER"));
        }

        @Test
        @DisplayName("응답에 비밀번호가 포함되지 않는다")
        void responseDoesNotContainPassword() throws Exception {
            // given
            MemberCreateRequest request = new MemberCreateRequest("secureuser", "password123", "보안유저");

            // when & then
            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.password").doesNotExist());
        }

        @Test
        @DisplayName("중복된 아이디로 가입하면 409를 반환한다")
        void duplicateUsername() throws Exception {
            // given - admin은 h2-data.sql에서 이미 등록됨
            MemberCreateRequest request = new MemberCreateRequest("admin", "password123", "중복유저");

            // when & then
            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디입니다: admin"));
        }

        @Test
        @DisplayName("아이디가 빈값이면 400을 반환한다")
        void blankUsername() throws Exception {
            // given
            MemberCreateRequest request = new MemberCreateRequest("", "password123", "홍길동");

            // when & then
            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'username')]").exists());
        }

        @Test
        @DisplayName("비밀번호가 3자 미만이면 400을 반환한다")
        void shortPassword() throws Exception {
            // given
            MemberCreateRequest request = new MemberCreateRequest("newuser", "ab", "홍길동");

            // when & then
            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
        }

        @Test
        @DisplayName("이름이 빈값이면 400을 반환한다")
        void blankName() throws Exception {
            // given
            MemberCreateRequest request = new MemberCreateRequest("newuser", "password123", "");

            // when & then
            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'name')]").exists());
        }

        @Test
        @DisplayName("모든 필드가 빈값이면 400 + 여러 에러를 반환한다")
        void allFieldsBlank() throws Exception {
            // given
            MemberCreateRequest request = new MemberCreateRequest("", "", "");

            // when & then
            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
        }

        @Test
        @DisplayName("인증 없이도 회원가입이 가능하다 (permitAll)")
        void noAuthRequired() throws Exception {
            // given
            MemberCreateRequest request = new MemberCreateRequest("publicuser", "password123", "공개유저");

            // when & then - Authorization 헤더 없이 요청
            mockMvc.perform(post("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
        }
    }
}
