package com.techeer.carpool.global.init;

import com.techeer.carpool.domain.comment.entity.Comment;
import com.techeer.carpool.domain.comment.repository.CommentRepository;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Profile("local")
@Component
@RequiredArgsConstructor
public class LocalDataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Member test  = createMemberIfNotExists("test@carpool.com",  "password1234", "테스트유저");
        Member admin = createMemberIfNotExists("admin@carpool.com", "admin1234!",   "관리자");

        if (postRepository.findByDeletedFalseOrderByCreatedAtDesc().isEmpty()) {
            seedPosts(test, admin);
        }

        log.info("[LocalDataInitializer] 초기 데이터 생성 완료");
    }

    private void seedPosts(Member test, Member admin) {
        Post p1 = postRepository.save(Post.builder()
                .memberId(admin.getId())
                .title("강남역 → 판교역")
                .departureLocation("강남역")
                .departureLat(37.4979).departureLng(127.0276)
                .destinationLocation("판교역")
                .destinationLat(37.3943).destinationLng(127.1110)
                .departureTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(30).withSecond(0).withNano(0))
                .maxPassengers(3)
                .description("정시 출발합니다. 짐 많으신 분은 미리 말씀해 주세요.")
                .autoAccept(false)
                .price(5000)
                .build());

        Post p2 = postRepository.save(Post.builder()
                .memberId(test.getId())
                .title("홍대입구 → 여의도역")
                .departureLocation("홍대입구")
                .departureLat(37.5572).departureLng(126.9247)
                .destinationLocation("여의도역")
                .destinationLat(37.5215).destinationLng(126.9242)
                .departureTime(LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0))
                .maxPassengers(2)
                .description("경유지 없이 직행입니다.")
                .autoAccept(true)
                .price(3000)
                .build());

        Post p3 = postRepository.save(Post.builder()
                .memberId(admin.getId())
                .title("서울역 → 수원역")
                .departureLocation("서울역")
                .departureLat(37.5547).departureLng(126.9707)
                .destinationLocation("수원역")
                .destinationLat(37.2664).destinationLng(127.0003)
                .departureTime(LocalDateTime.now().plusDays(3).withHour(7).withMinute(0).withSecond(0).withNano(0))
                .maxPassengers(4)
                .description("반드시 제시간에 탑승 부탁드립니다.")
                .autoAccept(false)
                .price(4000)
                .build());

        seedComments(p1, test, admin);
        seedComments(p2, admin, test);
        seedComments(p3, test, admin);
    }

    private void seedComments(Post post, Member first, Member second) {
        commentRepository.save(Comment.builder()
                .postId(post.getId())
                .memberId(first.getId())
                .content("저도 같은 방향이에요! 탑승 가능할까요?")
                .build());
        commentRepository.save(Comment.builder()
                .postId(post.getId())
                .memberId(second.getId())
                .content("몇 시쯤 도착 예정인가요?")
                .build());
        commentRepository.save(Comment.builder()
                .postId(post.getId())
                .memberId(first.getId())
                .content("좋아요, 당일 아침에 다시 연락 드릴게요 :)")
                .build());
    }

    private Member createMemberIfNotExists(String email, String password, String nickname) {
        return memberRepository.findByEmail(email).orElseGet(() ->
                memberRepository.save(Member.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .nickname(nickname)
                        .build()));
    }
}
