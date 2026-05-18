package com.techeer.carpool.domain.member.driver.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "drivers")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long vehicleOptionId;

    @Column(nullable = false, unique = true, length = 20)
    private String carNumber;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public Driver(Long memberId, Long vehicleOptionId, String carNumber) {
        this.memberId = memberId;
        this.vehicleOptionId = vehicleOptionId;
        this.carNumber = carNumber;
    }

    public void update(Long vehicleOptionId, String carNumber) {
        this.vehicleOptionId = vehicleOptionId;
        this.carNumber = carNumber;
    }

    public void softDelete() {
        this.deleted = true;
    }
}
