package com.techeer.carpool.domain.post;

import tools.jackson.databind.ObjectMapper;
import com.techeer.carpool.domain.auth.repository.BlacklistRedisRepository;
import com.techeer.carpool.domain.auth.repository.RefreshTokenRedisRepository;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.notification.publisher.RedisNotificationPublisher;
import com.techeer.carpool.domain.post.entity.Tag;
import com.techeer.carpool.domain.post.repository.PostRepository;
import com.techeer.carpool.domain.post.repository.TagRepository;
import com.techeer.carpool.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TagIntegrationTest {

    @MockitoBean RedisMessageListenerContainer listenerContainer;
    @MockitoBean BlacklistRedisRepository blacklistRedisRepository;
    @MockitoBean RefreshTokenRedisRepository refreshTokenRedisRepository;
    @MockitoBean RedisNotificationPublisher notificationPublisher;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired PostRepository postRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    private Long memberId;
    private String token;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        memberRepository.deleteAll();

        Member member = memberRepository.save(Member.builder()
                .email("tag-test@test.com").password("pw").nickname("태그테스터").build());
        memberId = member.getId();
        token = "Bearer " + jwtTokenProvider.createAccessToken(memberId);

        tag1 = tagRepository.save(Tag.builder().name("금연").build());
        tag2 = tagRepository.save(Tag.builder().name("조용한 분위기").build());
    }

    // ── GET /api/v1/tags ──────────────────────────────────────

    @Test
    @DisplayName("태그 목록 조회 - 인증 없이 200")
    void getAllTags_noAuth_200() throws Exception {
        mockMvc.perform(get("/api/v1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].name").exists());
    }

    // ── POST /api/v1/posts with tagIds ────────────────────────

    @Test
    @DisplayName("tagIds 포함 게시글 생성 - 응답 tags 배열에 id·name 반환")
    void createPost_withTagIds_returnsTagObjects() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postBody(List.of(tag1.getId(), tag2.getId())))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tags.length()").value(2))
                .andExpect(jsonPath("$.data.tags[0].id").exists())
                .andExpect(jsonPath("$.data.tags[0].name").exists());
    }

    @Test
    @DisplayName("tagIds 없이 게시글 생성 - tags 빈 배열")
    void createPost_noTagIds_emptyTagsArray() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postBody(List.of()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tags.length()").value(0));
    }

    // ── GET /api/v1/posts/{id} with tags ─────────────────────

    @Test
    @DisplayName("게시글 단건 조회 - tags 필드 포함")
    void getPost_includesTags() throws Exception {
        Long postId = createPostAndGetId(List.of(tag1.getId(), tag2.getId()));

        mockMvc.perform(get("/api/v1/posts/{id}", postId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags.length()").value(2));
    }

    // ── GET /api/v1/posts with tags ───────────────────────────

    @Test
    @DisplayName("게시글 목록 조회 - 각 게시글에 tags 포함")
    void getAllPosts_includesTags() throws Exception {
        createPostAndGetId(List.of(tag1.getId()));

        mockMvc.perform(get("/api/v1/posts")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tags").isArray())
                .andExpect(jsonPath("$.data[0].tags.length()").value(1));
    }

    // ── PUT /api/v1/posts/{id} tag 변경 ──────────────────────

    @Test
    @DisplayName("게시글 수정 - 태그 교체 (tag1 → tag2)")
    void updatePost_replaceTags() throws Exception {
        Long postId = createPostAndGetId(List.of(tag1.getId()));

        Map<String, Object> updateBody = postBody(List.of(tag2.getId()));
        updateBody.put("status", "OPEN");

        mockMvc.perform(put("/api/v1/posts/{id}", postId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags.length()").value(1))
                .andExpect(jsonPath("$.data.tags[0].name").value("조용한 분위기"));
    }

    @Test
    @DisplayName("게시글 수정 - 태그 전체 제거")
    void updatePost_clearTags() throws Exception {
        Long postId = createPostAndGetId(List.of(tag1.getId(), tag2.getId()));

        Map<String, Object> updateBody = postBody(List.of());
        updateBody.put("status", "OPEN");

        mockMvc.perform(put("/api/v1/posts/{id}", postId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags.length()").value(0));
    }

    // ── helpers ───────────────────────────────────────────────

    private Long createPostAndGetId(List<Long> tagIds) throws Exception {
        String resp = mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postBody(tagIds))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).path("data").path("id").asLong();
    }

    private Map<String, Object> postBody(List<Long> tagIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "태그 테스트 카풀");
        body.put("departureLocation", "강남역");
        body.put("departureLat", 37.4979);
        body.put("departureLng", 127.0276);
        body.put("destinationLocation", "판교역");
        body.put("destinationLat", 37.3943);
        body.put("destinationLng", 127.1110);
        body.put("departureTime", LocalDateTime.now().plusDays(1).toString());
        body.put("maxPassengers", 3);
        body.put("description", "테스트");
        body.put("autoAccept", false);
        body.put("tagIds", tagIds);
        return body;
    }
}
