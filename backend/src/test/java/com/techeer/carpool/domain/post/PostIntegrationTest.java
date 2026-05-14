package com.techeer.carpool.domain.post;

import tools.jackson.databind.ObjectMapper;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.post.entity.PostStatus;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.domain.auth.repository.BlacklistRedisRepository;
import com.techeer.carpool.domain.auth.repository.RefreshTokenRedisRepository;
import com.techeer.carpool.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PostRepository postRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @MockBean RefreshTokenRedisRepository refreshTokenRedisRepository;
    @MockBean BlacklistRedisRepository blacklistRedisRepository;
    @MockBean com.techeer.carpool.domain.notification.publisher.RedisNotificationPublisher notificationPublisher;

    private Long ownerId;
    private Long otherId;
    private Long postId;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        memberRepository.deleteAll();

        Member owner = memberRepository.save(Member.builder()
                .email("owner@test.com").password("pw").nickname("작성자").build());
        ownerId = owner.getId();
        ownerToken = "Bearer " + jwtTokenProvider.createAccessToken(ownerId);

        Member other = memberRepository.save(Member.builder()
                .email("other@test.com").password("pw").nickname("타인").build());
        otherId = other.getId();
        otherToken = "Bearer " + jwtTokenProvider.createAccessToken(otherId);

        Post post = postRepository.save(Post.builder()
                .memberId(ownerId)
                .title("강남 → 판교")
                .departureLocation("강남역").departureLat(37.4979).departureLng(127.0276)
                .destinationLocation("판교역").destinationLat(37.3943).destinationLng(127.1110)
                .departureTime(LocalDateTime.now().plusDays(1))
                .maxPassengers(3)
                .description("테스트")
                .autoAccept(false)
                .build());
        postId = post.getId();
    }

    // ── 게시글 생성 ──────────────────────────────────────────

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_success() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "새 카풀");
        body.put("departureLocation", "홍대입구");
        body.put("departureLat", 37.5572);
        body.put("departureLng", 126.9247);
        body.put("destinationLocation", "여의도");
        body.put("destinationLat", 37.5215);
        body.put("destinationLng", 126.9242);
        body.put("departureTime", LocalDateTime.now().plusDays(2).toString());
        body.put("maxPassengers", 2);
        body.put("description", "직행");
        body.put("autoAccept", false);

        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("새 카풀"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.currentPassengers").value(0))
                .andExpect(jsonPath("$.data.nickname").value("작성자"));
    }

    @Test
    @DisplayName("게시글 생성 실패 - 미인증")
    void createPost_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── 게시글 조회 ──────────────────────────────────────────

    @Test
    @DisplayName("게시글 전체 목록 조회 성공")
    void getAllPosts_success() throws Exception {
        mockMvc.perform(get("/api/v1/posts")
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("강남 → 판교"));
    }

    @Test
    @DisplayName("게시글 목록 조회 실패 - 미인증 시 401")
    void getAllPosts_unauthenticated_401() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void getPost_success() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{id}", postId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.title").value("강남 → 판교"));
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 존재하지 않는 ID")
    void getPost_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{id}", 99999L)
                        .header("Authorization", ownerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_001"));
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 삭제된 게시글")
    void getPost_deleted() throws Exception {
        Post post = postRepository.findById(postId).get();
        post.delete();
        postRepository.save(post);

        mockMvc.perform(get("/api/v1/posts/{id}", postId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_001"));
    }

    // ── 게시글 수정 ──────────────────────────────────────────

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() throws Exception {
        Map<String, Object> body = buildUpdateBody("수정된 제목", "수정된 내용");
        mockMvc.perform(put("/api/v1/posts/{id}", postId)
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 타인이 수정 시도")
    void updatePost_forbidden() throws Exception {
        Map<String, Object> body = buildUpdateBody("해킹된 제목", "");
        mockMvc.perform(put("/api/v1/posts/{id}", postId)
                        .header("Authorization", otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST_002"));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
    void updatePost_notFound() throws Exception {
        Map<String, Object> body = buildUpdateBody("없는 게시글", "");
        mockMvc.perform(put("/api/v1/posts/{id}", 99999L)
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_001"));
    }

    private Map<String, Object> buildUpdateBody(String title, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("departureLocation", "강남역");
        body.put("departureLat", 37.4979);
        body.put("departureLng", 127.0276);
        body.put("destinationLocation", "판교역");
        body.put("destinationLat", 37.3943);
        body.put("destinationLng", 127.1110);
        body.put("departureTime", LocalDateTime.now().plusDays(1).toString());
        body.put("maxPassengers", 3);
        body.put("description", description);
        body.put("autoAccept", false);
        body.put("status", "OPEN");
        return body;
    }

    // ── 게시글 삭제 ──────────────────────────────────────────

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/{id}", postId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."));

        // 삭제 후 조회 시 404
        mockMvc.perform(get("/api/v1/posts/{id}", postId)
                        .header("Authorization", ownerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 타인이 삭제 시도")
    void deletePost_forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/{id}", postId)
                        .header("Authorization", otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST_002"));
    }
}
