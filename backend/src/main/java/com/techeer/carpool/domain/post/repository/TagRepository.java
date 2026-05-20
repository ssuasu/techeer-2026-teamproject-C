package com.techeer.carpool.domain.post.repository;

import com.techeer.carpool.domain.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByIdIn(List<Long> ids);
}
