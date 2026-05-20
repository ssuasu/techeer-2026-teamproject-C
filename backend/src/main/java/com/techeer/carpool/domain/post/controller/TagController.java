package com.techeer.carpool.domain.post.controller;

import com.techeer.carpool.domain.post.dto.TagResponse;
import com.techeer.carpool.domain.post.service.TagReadService;
import com.techeer.carpool.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagReadService tagReadService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        return ResponseEntity.ok(ApiResponse.of("태그 목록 조회 성공", tagReadService.getAllTags()));
    }
}
