package com.malgn.contents.controller;

import com.malgn.common.dto.ApiResponse;
import com.malgn.common.dto.PageResponse;
import com.malgn.contents.dto.ContentsCreateRequest;
import com.malgn.contents.dto.ContentsResponse;
import com.malgn.contents.dto.ContentsUpdateRequest;
import com.malgn.contents.service.ContentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "콘텐츠", description = "콘텐츠 CRUD API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/contents")
public class ContentsController {

    private final ContentsService contentsService;

    @Operation(summary = "콘텐츠 생성", description = "새 콘텐츠를 등록합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
        })
    @PostMapping
    public ResponseEntity<ApiResponse<ContentsResponse>> create(
        @Valid @RequestBody ContentsCreateRequest request) {

        ContentsResponse response = contentsService.create(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created(response));
    }

    @Operation(summary = "콘텐츠 목록 조회", description = "페이징된 콘텐츠 목록을 반환합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
        })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ContentsResponse>>> getList(
        @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
        @ParameterObject
        Pageable pageable) {

        PageResponse<ContentsResponse> response = contentsService.getList(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "콘텐츠 상세 조회", description = "콘텐츠 상세 정보를 반환하고 조회수를 1 증가시킵니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "콘텐츠 없음")
        })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentsResponse>> getDetail(@PathVariable Long id) {
        ContentsResponse response = contentsService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "콘텐츠 수정", description = "콘텐츠를 수정합니다. 본인 또는 ADMIN만 가능합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "콘텐츠 없음")
        })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentsResponse>> update(
        @PathVariable Long id,
        @Valid @RequestBody ContentsUpdateRequest request) {

        ContentsResponse response = contentsService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "콘텐츠 삭제", description = "콘텐츠를 삭제합니다. 본인 또는 ADMIN만 가능합니다.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "콘텐츠 없음")
        })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
