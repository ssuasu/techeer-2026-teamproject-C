package com.techeer.carpool.global.init;

import com.techeer.carpool.domain.application.entity.Application;
import com.techeer.carpool.domain.application.repository.ApplicationRepository;
import com.techeer.carpool.domain.comment.entity.Comment;
import com.techeer.carpool.domain.comment.repository.CommentRepository;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;

import com.techeer.carpool.domain.vehicle.entity.CarColor;
import com.techeer.carpool.domain.vehicle.entity.VehicleOption;
import com.techeer.carpool.domain.vehicle.repository.VehicleOptionRepository;

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
    private final ApplicationRepository applicationRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final VehicleOptionRepository vehicleOptionRepository;

    @Override
    public void run(String... args) {
        Member test  = createMemberIfNotExists("test@carpool.com",  "password1234", "테스트유저");
        Member admin = createMemberIfNotExists("admin@carpool.com", "admin1234!",   "관리자");

        if (postRepository.findByDeletedFalseOrderByCreatedAtDesc().isEmpty()) {
            seedPosts(test, admin);
        }

        initVehicleOptions();
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

        // test 유저 소유 게시글 추가 (관리자가 신청할 대상)
        Post p4 = postRepository.save(Post.builder()
                .memberId(test.getId())
                .title("잠실역 → 강남역")
                .departureLocation("잠실역")
                .departureLat(37.5134).departureLng(127.1000)
                .destinationLocation("강남역")
                .destinationLat(37.4979).destinationLng(127.0276)
                .departureTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0))
                .maxPassengers(3)
                .description("퇴근길 카풀입니다. 편하게 연락 주세요.")
                .autoAccept(false)
                .price(3000)
                .build());

        Post p5 = postRepository.save(Post.builder()
                .memberId(test.getId())
                .title("신촌역 → 인천공항")
                .departureLocation("신촌역")
                .departureLat(37.5551).departureLng(126.9368)
                .destinationLocation("인천공항 T1")
                .destinationLat(37.4494).destinationLng(126.4508)
                .departureTime(LocalDateTime.now().plusDays(4).withHour(5).withMinute(30).withSecond(0).withNano(0))
                .maxPassengers(2)
                .description("새벽 출발이라 짐 많으신 분도 환영합니다.")
                .autoAccept(false)
                .price(20000)
                .build());

        seedApplications(test, admin, p2, p4, p5);

        seedComments(p1, test, admin);
        seedComments(p2, admin, test);
        seedComments(p3, test, admin);
    }

    private void seedApplications(Member test, Member admin, Post p2, Post p4, Post p5) {
        // 관리자 → p2 (홍대→여의도): PENDING — 테스트유저가 아직 처리 안 한 신청
        applicationRepository.save(Application.builder()
                .postId(p2.getId())
                .applicantId(admin.getId())
                .build());

        // 관리자 → p4 (잠실→강남): ACCEPTED — 수락된 신청, currentPassengers 반영
        Application acceptedApp = applicationRepository.save(Application.builder()
                .postId(p4.getId())
                .applicantId(admin.getId())
                .build());
        acceptedApp.accept();
        applicationRepository.save(acceptedApp);
        p4.incrementPassengers();
        postRepository.save(p4);

        // 관리자 → p5 (신촌→인천공항): REJECTED — 거절된 신청
        Application rejectedApp = applicationRepository.save(Application.builder()
                .postId(p5.getId())
                .applicantId(admin.getId())
                .build());
        rejectedApp.reject();
        applicationRepository.save(rejectedApp);
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

    private void initVehicleOptions() {
        if (vehicleOptionRepository.count() > 0) return;

        List<String[]> models = List.of(
                new String[]{"현대", "아반떼"},
                new String[]{"현대", "소나타"},
                new String[]{"현대", "그랜저"},
                new String[]{"기아", "K5"},
                new String[]{"기아", "스포티지"},
                new String[]{"기아", "카니발"},
                new String[]{"BMW", "3시리즈"},
                new String[]{"벤츠", "E클래스"}
        );

        List<VehicleOption> options = new java.util.ArrayList<>();
        for (String[] m : models) {
            for (CarColor color : CarColor.values()) {
                options.add(VehicleOption.builder()
                        .brand(m[0]).model(m[1]).color(color).build());
            }
        }
        vehicleOptionRepository.saveAll(options);
    }
}
