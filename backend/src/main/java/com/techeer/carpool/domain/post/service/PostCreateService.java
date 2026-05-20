package com.techeer.carpool.domain.post.service;

import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.post.dto.PostCreateRequest;
import com.techeer.carpool.domain.post.dto.PostResponse;
import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.entity.Tag;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.domain.post.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCreateService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;

    @Transactional
    public PostResponse createPost(PostCreateRequest request, Long memberId) {
        List<Tag> tags = tagRepository.findAllByIdIn(
                request.getTagIds() != null ? request.getTagIds() : List.of()
        );
        Post post = Post.builder()
                .memberId(memberId)
                .title(request.getTitle())
                .departureLocation(request.getDepartureLocation())
                .departureLat(request.getDepartureLat())
                .departureLng(request.getDepartureLng())
                .destinationLocation(request.getDestinationLocation())
                .destinationLat(request.getDestinationLat())
                .destinationLng(request.getDestinationLng())
                .departureTime(request.getDepartureTime())
                .maxPassengers(request.getMaxPassengers())
                .description(request.getDescription())
                .autoAccept(request.isAutoAccept())
                .price(request.getPrice())
                .tags(tags)
                .build();
        Post saved = postRepository.save(post);
        String nickname = memberRepository.findById(saved.getMemberId())
                .map(Member::getNickname)
                .orElse("알 수 없음");
        return PostResponse.from(saved, nickname);
    }
}
