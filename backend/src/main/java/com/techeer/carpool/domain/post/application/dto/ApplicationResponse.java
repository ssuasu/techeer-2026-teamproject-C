package com.techeer.carpool.domain.post.application.dto;

import com.techeer.carpool.domain.post.application.entity.Application;
import com.techeer.carpool.domain.post.application.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationResponse {

    private Long id;
    private Long postId;
    private Long applicantId;
    private String applicantNickname;
    private ApplicationStatus status;
    private LocalDateTime createdAt;

    public static ApplicationResponse of(Application application, String nickname) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .postId(application.getPostId())
                .applicantId(application.getApplicantId())
                .applicantNickname(nickname)
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
