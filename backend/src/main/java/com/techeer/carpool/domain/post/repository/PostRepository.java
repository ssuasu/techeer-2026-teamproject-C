package com.techeer.carpool.domain.post.repository;

import com.techeer.carpool.domain.post.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByDeletedFalseOrderByCreatedAtDesc();

    Optional<Post> findByIdAndDeletedFalse(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deleted = false")
    Optional<Post> findByIdAndDeletedFalseWithLock(@Param("id") Long id);
}