package com.malgn.member.dto;

import com.malgn.member.entity.Member;
import com.malgn.member.entity.Role;

public record MemberResponse(
    Long id,
    String username,
    String name,
    Role role
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getUsername(),
            member.getName(),
            member.getRole()
        );
    }
}
