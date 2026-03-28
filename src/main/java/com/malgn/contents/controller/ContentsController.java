package com.malgn.contents.controller;

import com.malgn.common.dto.ApiResponse;
import com.malgn.common.dto.PageResponse;
import com.malgn.contents.dto.ContentsCreateRequest;
import com.malgn.contents.dto.ContentsResponse;
import com.malgn.contents.dto.ContentsUpdateRequest;
import com.malgn.contents.service.ContentsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/contents")
public class ContentsController {

    private final ContentsService contentsService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContentsResponse>> create(
        @Valid @RequestBody ContentsCreateRequest request) {

        ContentsResponse response = contentsService.create(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ContentsResponse>>> getList(
        @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
        Pageable pageable) {

        PageResponse<ContentsResponse> response = contentsService.getList(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentsResponse>> getDetail(@PathVariable Long id) {
        ContentsResponse response = contentsService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentsResponse>> update(
        @PathVariable Long id,
        @Valid @RequestBody ContentsUpdateRequest request) {

        ContentsResponse response = contentsService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contentsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
