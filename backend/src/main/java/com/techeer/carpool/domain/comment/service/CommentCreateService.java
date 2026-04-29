package com.techeer.carpool.domain.comment.service;

import com.techeer.carpool.domain.comment.dto.CommentCreateRequest;
import com.techeer.carpool.domain.comment.dto.CommentResponse;
import com.techeer.carpool.domain.comment.entity.Comment;
import com.techeer.carpool.domain.comment.repository.CommentRepository;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentCreateService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request, Long memberId) {
        postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.builder()
                .postId(postId)
                .memberId(memberId)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        String nickname = memberRepository.findById(memberId)
                .map(Member::getNickname)
                .orElse("알 수 없음");
        return CommentResponse.from(saved, nickname);
    }
}
