package com.techeer.carpool.domain.member.service;

import com.techeer.carpool.domain.auth.repository.RefreshTokenRedisRepository;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWithdrawService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedFalse(memberId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        refreshTokenRedisRepository.delete(memberId);
    }
}
