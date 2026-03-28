package com.malgn.contents.service;

import com.malgn.common.dto.PageResponse;
import com.malgn.common.exception.EntityNotFoundException;
import com.malgn.contents.dto.ContentsCreateRequest;
import com.malgn.contents.dto.ContentsResponse;
import com.malgn.contents.dto.ContentsUpdateRequest;
import com.malgn.contents.entity.Contents;
import com.malgn.contents.repository.ContentsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ContentsServiceTest {

    @InjectMocks
    private ContentsService contentsService;

    @Mock
    private ContentsRepository contentsRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * SecurityContext에 인증 정보를 설정하는 헬퍼 메서드
     */
    private void setAuthentication(String username, String role) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                username, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 테스트용 Contents 엔티티를 생성하는 헬퍼 메서드
     * createdBy는 Auditing이 아닌 리플렉션으로 설정
     */
    private Contents createTestContents(Long id, String title, String description, String createdBy) {
        Contents contents = Contents.builder()
            .title(title)
            .description(description)
            .build();

        // 리플렉션으로 id, createdBy 설정 (테스트 전용)
        try {
            var idField = Contents.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(contents, id);

            var createdByField = Contents.class.getDeclaredField("createdBy");
            createdByField.setAccessible(true);
            createdByField.set(contents, createdBy);
        } catch (Exception e) {
            throw new RuntimeException("테스트 엔티티 생성 실패", e);
        }

        return contents;
    }

    @Nested
    @DisplayName("콘텐츠 생성")
    class Create {

        @Test
        @DisplayName("정상적으로 콘텐츠를 생성한다")
        void createSuccess() {
            // given
            ContentsCreateRequest request = new ContentsCreateRequest("제목", "내용");
            Contents saved = createTestContents(1L, "제목", "내용", "admin");

            given(contentsRepository.save(any(Contents.class))).willReturn(saved);

            // when
            ContentsResponse response = contentsService.create(request);

            // then
            assertThat(response.title()).isEqualTo("제목");
            assertThat(response.description()).isEqualTo("내용");
            verify(contentsRepository).save(any(Contents.class));
        }

        @Test
        @DisplayName("초기 조회수는 0이다")
        void initialViewCountIsZero() {
            // given
            ContentsCreateRequest request = new ContentsCreateRequest("제목", "내용");
            Contents saved = createTestContents(1L, "제목", "내용", "admin");

            given(contentsRepository.save(any(Contents.class))).willReturn(saved);

            // when
            ContentsResponse response = contentsService.create(request);

            // then
            assertThat(response.viewCount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("콘텐츠 목록 조회")
    class GetList {

        @Test
        @DisplayName("페이징된 목록을 반환한다")
        void getListWithPaging() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Contents contents1 = createTestContents(1L, "제목1", "내용1", "admin");
            Contents contents2 = createTestContents(2L, "제목2", "내용2", "user1");
            Page<Contents> page = new PageImpl<>(List.of(contents1, contents2), pageable, 2);

            given(contentsRepository.findAll(pageable)).willReturn(page);

            // when
            PageResponse<ContentsResponse> response = contentsService.getList(pageable);

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(response.page()).isEqualTo(0);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isEqualTo(2);
            assertThat(response.totalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 목록일 때 빈 페이지를 반환한다")
        void emptyList() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contents> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(contentsRepository.findAll(pageable)).willReturn(emptyPage);

            // when
            PageResponse<ContentsResponse> response = contentsService.getList(pageable);

            // then
            assertThat(response.content()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("콘텐츠 상세 조회")
    class GetDetail {

        @Test
        @DisplayName("조회 시 조회수가 1 증가한다")
        void viewCountIncreases() {
            // given
            Contents contents = createTestContents(1L, "제목", "내용", "admin");
            assertThat(contents.getViewCount()).isEqualTo(0L);

            given(contentsRepository.findById(1L)).willReturn(Optional.of(contents));

            // when
            ContentsResponse response = contentsService.getDetail(1L);

            // then
            assertThat(response.viewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("여러 번 조회하면 조회수가 누적된다")
        void viewCountAccumulates() {
            // given
            Contents contents = createTestContents(1L, "제목", "내용", "admin");
            given(contentsRepository.findById(1L)).willReturn(Optional.of(contents));

            // when
            contentsService.getDetail(1L);
            contentsService.getDetail(1L);
            ContentsResponse response = contentsService.getDetail(1L);

            // then
            assertThat(response.viewCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 EntityNotFoundException을 던진다")
        void notFoundThrowsException() {
            // given
            given(contentsRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> contentsService.getDetail(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("콘텐츠를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("콘텐츠 수정")
    class Update {

        @Test
        @DisplayName("본인 콘텐츠를 수정할 수 있다")
        void ownerCanUpdate() {
            // given
            setAuthentication("user1", "USER");
            Contents contents = createTestContents(1L, "원래 제목", "원래 내용", "user1");
            ContentsUpdateRequest request = new ContentsUpdateRequest("수정 제목", "수정 내용");

            given(contentsRepository.findById(1L)).willReturn(Optional.of(contents));

            // when
            ContentsResponse response = contentsService.update(1L, request);

            // then
            assertThat(response.title()).isEqualTo("수정 제목");
            assertThat(response.description()).isEqualTo("수정 내용");
        }

        @Test
        @DisplayName("ADMIN은 다른 사용자의 콘텐츠를 수정할 수 있다")
        void adminCanUpdateOthers() {
            // given
            setAuthentication("admin", "ADMIN");
            Contents contents = createTestContents(1L, "원래 제목", "원래 내용", "user1");
            ContentsUpdateRequest request = new ContentsUpdateRequest("관리자 수정", "관리자 내용");

            given(contentsRepository.findById(1L)).willReturn(Optional.of(contents));

            // when
            ContentsResponse response = contentsService.update(1L, request);

            // then
            assertThat(response.title()).isEqualTo("관리자 수정");
        }

        @Test
        @DisplayName("다른 사용자의 콘텐츠를 수정하면 AccessDeniedException을 던진다")
        void otherUserCannotUpdate() {
            // given
            setAuthentication("user2", "USER");
            Contents contents = createTestContents(1L, "원래 제목", "원래 내용", "user1");
            ContentsUpdateRequest request = new ContentsUpdateRequest("해킹", "시도");

            given(contentsRepository.findById(1L)).willReturn(Optional.of(contents));

            // when & then
            assertThatThrownBy(() -> contentsService.update(1L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("본인이 작성한 콘텐츠만");
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠를 수정하면 EntityNotFoundException을 던진다")
        void updateNotFound() {
            // given
            setAuthentication("admin", "ADMIN");
            ContentsUpdateRequest request = new ContentsUpdateRequest("수정", "내용");

            given(contentsRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> contentsService.update(999L, request))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("콘텐츠 삭제")
    class Delete {

        @Test
        @DisplayName("본인 콘텐츠를 삭제할 수 있다")
        void ownerCanDelete() {
            // given
            setAuthentication("user1", "USER");
            Contents contents = createTestContents(1L, "제목", "내용", "user1");

            given(contentsRepository.findById(1L)).willReturn(Optional.of(contents));

            // when
            contentsService.delete(1L);

            // then
            verify(contentsRepository).delete(contents);
        }

        @Test
        @DisplayName("ADMIN은 다른 사용자의 콘텐츠를 삭제할 수 있다")
        void adminCanDeleteOthers() {
            // given
            setAuthentication("admin", "ADMIN");
            Contents contents = createTestContents(1L, "제목", "내용", "user1");

            given(contentsRepository.findById(1L)).willReturn(Optional.of(contents));

            // when
            contentsService.delete(1L);

            // then
            verify(contentsRepository).delete(contents);
        }

        @Test
        @DisplayName("다른 사용자의 콘텐츠를 삭제하면 AccessDeniedException을 던진다")
        void otherUserCannotDelete() {
            // given
            setAuthentication("user2", "USER");
            Contents contents = createTestContents(1L, "제목", "내용", "user1");

            given(contentsRepository.findById(1L)).willReturn(Optional.of(contents));

            // when & then
            assertThatThrownBy(() -> contentsService.delete(1L))
                .isInstanceOf(AccessDeniedException.class);

            verify(contentsRepository, never()).delete(any(Contents.class));
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠를 삭제하면 EntityNotFoundException을 던진다")
        void deleteNotFound() {
            // given
            setAuthentication("admin", "ADMIN");
            given(contentsRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> contentsService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
