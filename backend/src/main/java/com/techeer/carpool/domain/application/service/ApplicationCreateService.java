package com.techeer.carpool.domain.application.service;

import com.techeer.carpool.domain.application.dto.ApplicationResponse;
import com.techeer.carpool.domain.application.entity.Application;
import com.techeer.carpool.domain.application.entity.ApplicationStatus;
import com.techeer.carpool.domain.application.repository.ApplicationRepository;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationCreateService {

    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ApplicationResponse apply(Long postId, Long applicantId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));

        if (post.getMemberId().equals(applicantId)) {
            throw new CarpoolException(ErrorCode.APPLICATION_SELF);
        }

        if (post.isFull()) {
            throw new CarpoolException(ErrorCode.APPLICATION_POST_FULL);
        }

        if (applicationRepository.existsByPostIdAndApplicantId(postId, applicantId)) {
            throw new CarpoolException(ErrorCode.APPLICATION_DUPLICATE);
        }

        Application application = Application.builder()
                .postId(postId)
                .applicantId(applicantId)
                .build();

        if (post.isAutoAccept()) {
            application.accept();
            post.incrementPassengers();
        }

        Application saved = applicationRepository.save(application);

        String nickname = memberRepository.findById(applicantId)
                .map(Member::getNickname)
                .orElse("알 수 없음");

        return ApplicationResponse.of(saved, nickname);
    }
}
