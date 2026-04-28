package com.techeer.carpool.domain.post.service;

import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.post.dto.PostResponse;
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
@Transactional(readOnly = true)
public class PostReadService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findByDeletedFalseOrderByCreatedAtDesc();
        Set<Long> memberIds = posts.stream().map(Post::getMemberId).collect(Collectors.toSet());
        Map<Long, String> nicknameMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));
        return posts.stream()
                .map(p -> PostResponse.from(p, nicknameMap.getOrDefault(p.getMemberId(), "알 수 없음")))
                .collect(Collectors.toList());
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));
        String nickname = memberRepository.findById(post.getMemberId())
                .map(Member::getNickname)
                .orElse("알 수 없음");
        return PostResponse.from(post, nickname);
    }
}
