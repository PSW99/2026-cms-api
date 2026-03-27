package com.malgn.member.service;

import com.malgn.common.exception.DuplicateResourceException;
import com.malgn.member.dto.MemberCreateRequest;
import com.malgn.member.dto.MemberResponse;
import com.malgn.member.entity.Member;
import com.malgn.member.entity.Role;
import com.malgn.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입")
    class CreateMember {

        @Test
        @DisplayName("정상적으로 회원을 생성한다")
        void createMemberSuccess() {
            // given
            MemberCreateRequest request = new MemberCreateRequest("newuser", "password123", "홍길동");

            given(memberRepository.existsByUsername("newuser")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("$2a$10$encodedPassword");
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
                Member member = invocation.getArgument(0);
                // 저장 후 ID가 부여된 것처럼 시뮬레이션
                return Member.builder()
                    .username(member.getUsername())
                    .password(member.getPassword())
                    .name(member.getName())
                    .role(member.getRole())
                    .build();
            });

            // when
            MemberResponse response = memberService.createMember(request);

            // then
            assertThat(response.username()).isEqualTo("newuser");
            assertThat(response.name()).isEqualTo("홍길동");
            assertThat(response.role()).isEqualTo(Role.USER);
            verify(memberRepository).save(any(Member.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("비밀번호가 암호화되어 저장된다")
        void passwordIsEncoded() {
            // given
            MemberCreateRequest request = new MemberCreateRequest("newuser", "rawPassword", "홍길동");
            String encodedPassword = "$2a$10$encodedPasswordHash";

            given(memberRepository.existsByUsername("newuser")).willReturn(false);
            given(passwordEncoder.encode("rawPassword")).willReturn(encodedPassword);
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            memberService.createMember(request);

            // then
            verify(passwordEncoder).encode("rawPassword");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("기본 역할은 USER이다")
        void defaultRoleIsUser() {
            // given
            MemberCreateRequest request = new MemberCreateRequest("newuser", "password123", "홍길동");

            given(memberRepository.existsByUsername("newuser")).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encoded");
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            MemberResponse response = memberService.createMember(request);

            // then
            assertThat(response.role()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("중복된 username이면 DuplicateResourceException을 던진다")
        void duplicateUsernameThrowsException() {
            // given
            MemberCreateRequest request = new MemberCreateRequest("admin", "password123", "중복유저");
            given(memberRepository.existsByUsername("admin")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("이미 존재하는 아이디입니다");

            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("중복 체크 실패 시 비밀번호 암호화가 호출되지 않는다")
        void noEncodeWhenDuplicate() {
            // given
            MemberCreateRequest request = new MemberCreateRequest("admin", "password123", "중복유저");
            given(memberRepository.existsByUsername("admin")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(DuplicateResourceException.class);

            verify(passwordEncoder, never()).encode(anyString());
        }
    }
}
