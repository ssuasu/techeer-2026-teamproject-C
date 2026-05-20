package com.techeer.carpool.domain.member.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileUpdateRequest {

    @Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하여야 합니다.")
    private String nickname;

    private String currentPassword;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String newPassword;

    @AssertTrue(message = "비밀번호 변경 시 현재 비밀번호가 필요합니다.")
    public boolean isPasswordChangeValid() {
        return newPassword == null || (currentPassword != null && !currentPassword.isBlank());
    }
}
