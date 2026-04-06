package com.techeer.carpool.domain.member.controller;

import com.techeer.carpool.domain.member.dto.LoginRequest;
import com.techeer.carpool.domain.member.dto.SignupRequest;
import com.techeer.carpool.domain.member.dto.TokenResponse;
import com.techeer.carpool.domain.member.service.MemberLoginService;
import com.techeer.carpool.domain.member.service.MemberSignupService;
import com.techeer.carpool.domain.member.service.MemberWithdrawService;
import com.techeer.carpool.domain.member.service.TokenReissueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberSignupService memberSignupService;
    private final MemberLoginService memberLoginService;
    private final TokenReissueService tokenReissueService;
    private final MemberWithdrawService memberWithdrawService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        memberSignupService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(memberLoginService.login(request));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(tokenReissueService.reissue(refreshToken));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        memberWithdrawService.withdraw(memberId);
        return ResponseEntity.noContent().build();
    }
}
