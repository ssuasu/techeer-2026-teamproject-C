package com.techeer.carpool.domain.application.service;

import com.techeer.carpool.domain.application.dto.ApplicationResponse;
import com.techeer.carpool.domain.application.entity.Application;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationReadService {

    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(Long applicantId) {
        List<Application> applications = applicationRepository
                .findByApplicantIdOrderByCreatedAtDesc(applicantId);

        String nickname = memberRepository.findById(applicantId)
                .map(Member::getNickname)
                .orElse("알 수 없음");

        return applications.stream()
                .map(a -> ApplicationResponse.of(a, nickname))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByPost(Long postId, Long requesterId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));

        if (!post.getMemberId().equals(requesterId)) {
            throw new CarpoolException(ErrorCode.APPLICATION_FORBIDDEN);
        }

        List<Application> applications = applicationRepository
                .findByPostIdOrderByCreatedAtAsc(postId);

        Set<Long> applicantIds = applications.stream()
                .map(Application::getApplicantId)
                .collect(Collectors.toSet());

        Map<Long, String> nicknameMap = memberRepository.findAllById(applicantIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        return applications.stream()
                .map(a -> ApplicationResponse.of(a, nicknameMap.getOrDefault(a.getApplicantId(), "알 수 없음")))
                .collect(Collectors.toList());
    }
}
