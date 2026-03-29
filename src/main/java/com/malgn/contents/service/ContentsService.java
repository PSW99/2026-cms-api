package com.malgn.contents.service;

import com.malgn.common.dto.PageResponse;
import com.malgn.common.exception.EntityNotFoundException;
import com.malgn.contents.dto.ContentsCreateRequest;
import com.malgn.contents.dto.ContentsResponse;
import com.malgn.contents.dto.ContentsUpdateRequest;
import com.malgn.contents.entity.Contents;
import com.malgn.contents.repository.ContentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ContentsService {

    private final ContentsRepository contentsRepository;

    @Transactional
    public ContentsResponse create(ContentsCreateRequest request) {
        Contents contents = Contents.builder()
                .title(request.title())
                .description(request.description())
                .build();

        Contents saved = contentsRepository.save(contents);
        return ContentsResponse.from(saved);
    }

    public PageResponse<ContentsResponse> getList(Pageable pageable) {
        Page<ContentsResponse> page = contentsRepository.findAll(pageable)
                .map(ContentsResponse::from);
        return PageResponse.from(page);
    }

    @Transactional
    public ContentsResponse getDetail(Long id) {
        Contents contents = findContentsById(id);
        contents.increaseViewCount();
        return ContentsResponse.from(contents);
    }

    @Transactional
    public ContentsResponse update(Long id, ContentsUpdateRequest request) {
        Contents contents = findContentsById(id);
        validateOwnerOrAdmin(contents);

        contents.update(request.title(), request.description());
        return ContentsResponse.from(contents);
    }

    @Transactional
    public void delete(Long id) {
        Contents contents = findContentsById(id);
        validateOwnerOrAdmin(contents);
        contents.softDelete();
    }

    @Transactional
    public ContentsResponse restore(Long id) {
        Contents contents = contentsRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("콘텐츠를 찾을 수 없습니다. id=" + id));

        if (!contents.getDeleted()) {
            throw new IllegalArgumentException("삭제되지 않은 콘텐츠입니다.");
        }

        validateAdminOnly();
        contents.restore();
        return ContentsResponse.from(contents);
    }

    public PageResponse<ContentsResponse> getDeletedList(Pageable pageable) {
        validateAdminOnly();
        Page<ContentsResponse> page = contentsRepository.findAllDeleted(pageable)
                .map(ContentsResponse::from);
        return PageResponse.from(page);
    }

    private void validateAdminOnly() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!isAdmin) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }
    }

    private Contents findContentsById(Long id) {
        return contentsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("콘텐츠를 찾을 수 없습니다. id=" + id));
    }

    private void validateOwnerOrAdmin(Contents contents) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (String) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin) {
            return;
        }

        if (!contents.getCreatedBy().equals(currentUsername)) {
            throw new AccessDeniedException("본인이 작성한 콘텐츠만 수정/삭제할 수 있습니다.");
        }
    }
}
