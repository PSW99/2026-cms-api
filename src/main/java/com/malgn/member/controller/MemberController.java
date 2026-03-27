package com.malgn.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.malgn.common.dto.ApiResponse;
import com.malgn.member.dto.MemberCreateRequest;
import com.malgn.member.dto.MemberResponse;
import com.malgn.member.service.MemberService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(
            @Valid @RequestBody MemberCreateRequest request) {

        MemberResponse response = memberService.createMember(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }
}
