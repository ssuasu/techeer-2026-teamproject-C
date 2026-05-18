package com.techeer.carpool.domain.post.comment.controller;

import com.techeer.carpool.domain.post.comment.dto.CommentCreateRequest;
import com.techeer.carpool.domain.post.comment.dto.CommentResponse;
import com.techeer.carpool.domain.post.comment.service.CommentCreateService;
import com.techeer.carpool.domain.post.comment.service.CommentDeleteService;
import com.techeer.carpool.domain.post.comment.service.CommentReadService;
import com.techeer.carpool.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentCreateService commentCreateService;
    private final CommentReadService commentReadService;
    private final CommentDeleteService commentDeleteService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("댓글이 작성되었습니다.", commentCreateService.createComment(postId, request, memberId)));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.of("댓글 목록 조회 성공", commentReadService.getCommentsByPostId(postId)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        commentDeleteService.deleteComment(commentId, memberId);
        return ResponseEntity.ok(ApiResponse.of("댓글이 삭제되었습니다."));
    }
}
