package com.techeer.carpool.domain.post.dto;

import com.techeer.carpool.domain.post.entity.Tag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {

    private Long id;
    private String name;

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
