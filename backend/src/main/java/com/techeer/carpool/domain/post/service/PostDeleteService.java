package com.techeer.carpool.domain.post.service;

import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostDeleteService {

    private final PostRepository postRepository;

    @Transactional
    public void deletePost(Long id, Long requesterId) {
        Post post = postRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CarpoolException(ErrorCode.POST_NOT_FOUND));
        if (!post.getMemberId().equals(requesterId)) {
            throw new CarpoolException(ErrorCode.POST_FORBIDDEN);
        }
        post.delete();
    }
}
