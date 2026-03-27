package com.malgn.auth.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // 테스트용 256bit 시크릿 (Base64 인코딩)
    private static final String TEST_SECRET =
        Base64.getEncoder().encodeToString("test-secret-key-for-jwt-unit-test-256bit!!".getBytes());
    private static final long EXPIRATION_MS = 3600000L; // 1시간

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, EXPIRATION_MS);
    }

    @Nested
    @DisplayName("토큰 생성")
    class GenerateToken {

        @Test
        @DisplayName("유효한 JWT 토큰을 생성한다")
        void generateValidToken() {
            // when
            String token = jwtTokenProvider.generateToken("admin", "ADMIN");

            // then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
        }

        @Test
        @DisplayName("토큰에 username이 포함된다")
        void tokenContainsUsername() {
            // when
            String token = jwtTokenProvider.generateToken("testuser", "USER");

            // then
            assertThat(jwtTokenProvider.getUsername(token)).isEqualTo("testuser");
        }

        @Test
        @DisplayName("토큰에 role이 포함된다")
        void tokenContainsRole() {
            // when
            String token = jwtTokenProvider.generateToken("admin", "ADMIN");

            // then
            assertThat(jwtTokenProvider.getRole(token)).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class ValidateToken {

        @Test
        @DisplayName("유효한 토큰은 true를 반환한다")
        void validToken() {
            // given
            String token = jwtTokenProvider.generateToken("admin", "ADMIN");

            // when & then
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰은 false를 반환한다")
        void expiredToken() {
            // given - 만료시간 0ms로 즉시 만료 토큰 생성
            JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, 0L);
            String token = expiredProvider.generateToken("admin", "ADMIN");

            // when & then
            assertThat(jwtTokenProvider.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("변조된 토큰은 false를 반환한다")
        void tamperedToken() {
            // given
            String token = jwtTokenProvider.generateToken("admin", "ADMIN");
            String tamperedToken = token + "tampered";

            // when & then
            assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
        }

        @Test
        @DisplayName("잘못된 형식의 토큰은 false를 반환한다")
        void malformedToken() {
            // when & then
            assertThat(jwtTokenProvider.validateToken("not.a.valid.jwt")).isFalse();
        }

        @Test
        @DisplayName("null 토큰은 false를 반환한다")
        void nullToken() {
            // when & then
            assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("다른 시크릿으로 서명된 토큰은 false를 반환한다")
        void differentSecretToken() {
            // given
            String otherSecret = Base64.getEncoder()
                .encodeToString("another-secret-key-for-different-signer!!".getBytes());
            JwtTokenProvider otherProvider = new JwtTokenProvider(otherSecret, EXPIRATION_MS);
            String token = otherProvider.generateToken("admin", "ADMIN");

            // when & then
            assertThat(jwtTokenProvider.validateToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 파싱")
    class ParseToken {

        @Test
        @DisplayName("서로 다른 사용자의 토큰은 서로 다른 username을 반환한다")
        void differentUsersReturnDifferentUsernames() {
            // given
            String adminToken = jwtTokenProvider.generateToken("admin", "ADMIN");
            String userToken = jwtTokenProvider.generateToken("user1", "USER");

            // when & then
            assertThat(jwtTokenProvider.getUsername(adminToken)).isEqualTo("admin");
            assertThat(jwtTokenProvider.getUsername(userToken)).isEqualTo("user1");
        }

        @Test
        @DisplayName("ADMIN과 USER 역할이 올바르게 파싱된다")
        void rolesAreParsedCorrectly() {
            // given
            String adminToken = jwtTokenProvider.generateToken("admin", "ADMIN");
            String userToken = jwtTokenProvider.generateToken("user1", "USER");

            // when & then
            assertThat(jwtTokenProvider.getRole(adminToken)).isEqualTo("ADMIN");
            assertThat(jwtTokenProvider.getRole(userToken)).isEqualTo("USER");
        }
    }
}
