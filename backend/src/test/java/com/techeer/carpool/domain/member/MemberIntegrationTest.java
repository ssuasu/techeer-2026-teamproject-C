package com.techeer.carpool.domain.member;

import tools.jackson.databind.ObjectMapper;
import com.techeer.carpool.domain.auth.repository.BlacklistRedisRepository;
import com.techeer.carpool.domain.auth.repository.RefreshTokenRedisRepository;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired PasswordEncoder passwordEncoder;

    @Mock RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Mock BlacklistRedisRepository blacklistRedisRepository;

    private Long memberId;
    private Long otherMemberId;
    private String token;
    private String otherToken;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        Member member = memberRepository.save(Member.builder()
                .email("me@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("나").build());
        memberId = member.getId();
        token = "Bearer " + jwtTokenProvider.createAccessToken(memberId);

        Member other = memberRepository.save(Member.builder()
                .email("other@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("타인").build());
        otherMemberId = other.getId();
        otherToken = "Bearer " + jwtTokenProvider.createAccessToken(otherMemberId);
    }

    // ── 프로필 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("내 프로필 조회 성공")
    void getMyProfile_success() throws Exception {
        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("나"));
    }

    @Test
    @DisplayName("프로필 조회 실패 - 미인증")
    void getMyProfile_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ID로 내 프로필 조회 성공")
    void getProfileById_self() throws Exception {
        mockMvc.perform(get("/api/v1/members/{id}", memberId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(memberId));
    }

    @Test
    @DisplayName("ID로 타인 프로필 조회 실패 - 403")
    void getProfileById_other_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/members/{id}", otherMemberId)
                        .header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MEMBER_001"));
    }

    // ── 프로필 수정 ──────────────────────────────────────────

    @Test
    @DisplayName("닉네임 수정 성공")
    void updateProfile_nickname() throws Exception {
        mockMvc.perform(put("/api/v1/members/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nickname", "새닉네임"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("새닉네임"));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updateProfile_password() throws Exception {
        mockMvc.perform(put("/api/v1/members/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "password123",
                                "newPassword", "newPassword456"
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updateProfile_wrongCurrentPassword() throws Exception {
        mockMvc.perform(put("/api/v1/members/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "wrongPassword",
                                "newPassword", "newPassword456"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_003"));
    }
}
