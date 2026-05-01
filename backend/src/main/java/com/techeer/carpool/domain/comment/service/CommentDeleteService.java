package com.techeer.carpool.domain.comment.service;

import com.techeer.carpool.domain.comment.repository.CommentRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentDeleteService {

    private final CommentRepository commentRepository;

    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        var comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMemberId().equals(memberId)) {
            throw new CarpoolException(ErrorCode.COMMENT_FORBIDDEN);
        }

        comment.delete();
    }
}
