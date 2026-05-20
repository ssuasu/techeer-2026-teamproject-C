package com.techeer.carpool.domain.member.entity;

import com.techeer.carpool.global.common.entity.SoftDeletableEntity;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Member extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private int totalRatingSum = 0;

    @Column(nullable = false)
    private int reviewCount = 0;

    @Builder
    public Member(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new CarpoolException(ErrorCode.INVALID_INPUT);
        }
        this.totalRatingSum += rating;
        this.reviewCount++;
    }

    public double getAverageRating() {
        return reviewCount == 0 ? 0.0 : (double) totalRatingSum / reviewCount;
    }

    public void updateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new CarpoolException(ErrorCode.INVALID_INPUT);
        }
        this.nickname = nickname;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void withdraw() {
        delete();
    }
}
