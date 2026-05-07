package com.techeer.carpool.domain.post.entity;

import com.techeer.carpool.global.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Post extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 100)
    private String departureLocation;

    private Double departureLat;

    private Double departureLng;

    @Column(nullable = false, length = 100)
    private String destinationLocation;

    private Double destinationLat;

    private Double destinationLng;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private int maxPassengers;

    @Column(nullable = false)
    private int currentPassengers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean autoAccept;

    private Integer price;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        this.status = PostStatus.OPEN;
        this.currentPassengers = 0;
    }

    @Builder
    public Post(Long memberId, String title,
                String departureLocation, Double departureLat, Double departureLng,
                String destinationLocation, Double destinationLat, Double destinationLng,
                LocalDateTime departureTime, int maxPassengers,
                String description, boolean autoAccept,
                Integer price, List<String> tags) {
        this.memberId = memberId;
        this.title = title;
        this.departureLocation = departureLocation;
        this.departureLat = departureLat;
        this.departureLng = departureLng;
        this.destinationLocation = destinationLocation;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.departureTime = departureTime;
        this.maxPassengers = maxPassengers;
        this.description = description;
        this.autoAccept = autoAccept;
        this.price = price;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public void close() {
        if (this.status == PostStatus.CLOSED) {
            throw new IllegalStateException("이미 마감된 게시글입니다.");
        }
        this.status = PostStatus.CLOSED;
    }

    public boolean isFull() {
        return this.currentPassengers >= this.maxPassengers;
    }

    public void incrementPassengers() {
        this.currentPassengers++;
        if (this.currentPassengers >= this.maxPassengers) {
            this.status = PostStatus.CLOSED;
        }
    }

    public void decrementPassengers() {
        if (this.currentPassengers > 0) {
            this.currentPassengers--;
        }
        if (this.status == PostStatus.CLOSED && this.currentPassengers < this.maxPassengers) {
            this.status = PostStatus.OPEN;
        }
    }

    public void refreshDepartureTime(LocalDateTime time) {
        this.departureTime = time;
    }

    public void updateFrom(PostUpdateCommand command) {
        this.title = command.title();
        this.departureLocation = command.departureLocation();
        this.departureLat = command.departureLat();
        this.departureLng = command.departureLng();
        this.destinationLocation = command.destinationLocation();
        this.destinationLat = command.destinationLat();
        this.destinationLng = command.destinationLng();
        this.departureTime = command.departureTime();
        this.maxPassengers = command.maxPassengers();
        if (command.description() != null) this.description = command.description();
        this.autoAccept = command.autoAccept();
        this.status = command.status();
        this.price = command.price();
        this.tags = command.tags() != null ? new ArrayList<>(command.tags()) : new ArrayList<>();
    }
}
