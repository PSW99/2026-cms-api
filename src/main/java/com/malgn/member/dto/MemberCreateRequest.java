package com.malgn.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 3, max = 50, message = "아이디는 3~50자로 입력해주세요.")
    String username,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 72, message = "비밀번호는 4~72자로 입력해주세요.")
    String password,

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    String name
) {
}
