package com.techeer.carpool.domain.post.service;

import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.post.dto.PostResponse;
import com.techeer.carpool.domain.post.dto.PostUpdateRequest;
import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.entity.Tag;
import com.techeer.carpool.domain.post.entity.PostUpdateCommand;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.domain.post.repository.TagRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostUpdateService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;

    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request, Long requesterId) {
        Post post = postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));
        if (!post.getMemberId().equals(requesterId)) {
            throw new CarpoolException(ErrorCode.POST_FORBIDDEN);
        }
        List<Tag> tags = tagRepository.findAllByIdIn(
                request.getTagIds() != null ? request.getTagIds() : List.of()
        );
        post.updateFrom(new PostUpdateCommand(
                request.getTitle(),
                request.getDepartureLocation(),
                request.getDepartureLat(),
                request.getDepartureLng(),
                request.getDestinationLocation(),
                request.getDestinationLat(),
                request.getDestinationLng(),
                request.getDepartureTime(),
                request.getMaxPassengers(),
                request.getDescription(),
                request.isAutoAccept(),
                request.getStatus(),
                request.getPrice(),
                tags
        ));
        String nickname = memberRepository.findById(post.getMemberId())
                .map(Member::getNickname)
                .orElse("알 수 없음");
        return PostResponse.from(post, nickname);
    }
}
