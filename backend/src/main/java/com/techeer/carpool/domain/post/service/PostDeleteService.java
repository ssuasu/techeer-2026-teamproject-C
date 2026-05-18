package com.techeer.carpool.domain.post.service;

import com.techeer.carpool.domain.post.application.entity.Application;
import com.techeer.carpool.domain.post.application.entity.ApplicationStatus;
import com.techeer.carpool.domain.post.application.repository.ApplicationRepository;
import com.techeer.carpool.domain.notification.dto.NotificationPayload;
import com.techeer.carpool.domain.notification.publisher.RedisNotificationPublisher;
import com.techeer.carpool.domain.notification.type.NotificationType;
import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostDeleteService {

    private final PostRepository postRepository;
    private final ApplicationRepository applicationRepository;
    private final RedisNotificationPublisher notificationPublisher;

    @Transactional
    public void deletePost(Long id, Long requesterId) {
        Post post = postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));
        if (!post.getMemberId().equals(requesterId)) {
            throw new CarpoolException(ErrorCode.POST_FORBIDDEN);
        }

        // 수락된 신청자들에게 POST_CANCELLED 알림 발송 후 소프트 딜리트
        List<Long> applicantIds = applicationRepository
                .findByPostIdAndStatus(id, ApplicationStatus.ACCEPTED)
                .stream()
                .map(Application::getApplicantId)
                .collect(Collectors.toList());

        notificationPublisher.publishToMany(applicantIds, NotificationPayload.builder()
                .type(NotificationType.POST_CANCELLED)
                .message("신청한 카풀 게시글이 취소되었습니다.")
                .data(Map.of("postId", id))
                .build());

        post.delete();
    }
}
