package com.malgn.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.malgn.auth.dto.LoginRequest;
import com.malgn.auth.dto.LoginResponse;
import com.malgn.auth.provider.JwtTokenProvider;
import com.malgn.common.dto.ApiResponse;
import com.malgn.member.entity.Member;
import com.malgn.member.repository.MemberRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request) {

        Member member = memberRepository.findByUsername(request.username())
            .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.generateToken(
            member.getUsername(),
            member.getRole().name()
        );

        return ResponseEntity.ok(ApiResponse.ok(LoginResponse.of(token)));
    }
}
