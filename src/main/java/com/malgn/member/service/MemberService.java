package com.malgn.member.service;

import com.malgn.common.exception.DuplicateResourceException;
import com.malgn.member.dto.MemberCreateRequest;
import com.malgn.member.dto.MemberResponse;
import com.malgn.member.entity.Member;
import com.malgn.member.entity.Role;
import com.malgn.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("이미 존재하는 아이디입니다: " + request.username());
        }

        Member member = Member.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .role(Role.USER)
            .build();

        Member savedMember = memberRepository.save(member);
        return MemberResponse.from(savedMember);
    }
}
