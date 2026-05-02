package com.techeer.carpool.domain.driver;

import tools.jackson.databind.ObjectMapper;
import com.techeer.carpool.domain.driver.repository.DriverRepository;
import com.techeer.carpool.domain.member.entity.Member;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.vehicle.entity.VehicleOption;
import com.techeer.carpool.domain.vehicle.entity.VehicleOptionType;
import com.techeer.carpool.domain.vehicle.repository.VehicleOptionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DriverIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired DriverRepository driverRepository;
    @Autowired VehicleOptionRepository vehicleOptionRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private Long carModelId;
    private Long carColorId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        driverRepository.deleteAll();
        memberRepository.deleteAll();
        vehicleOptionRepository.deleteAll();

        VehicleOption carModel = vehicleOptionRepository.save(VehicleOption.builder()
                .type(VehicleOptionType.MODEL).brand("현대").name("아반떼").build());
        VehicleOption carColor = vehicleOptionRepository.save(VehicleOption.builder()
                .type(VehicleOptionType.COLOR).name("흰색").hexCode("#FFFFFF").build());
        carModelId = carModel.getId();
        carColorId = carColor.getId();

        memberRepository.save(Member.builder()
                .email("driver@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("테스트드라이버")
                .build());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "driver@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(body).at("/data/accessToken").asText();
    }

    @AfterEach
    void tearDown() {
        driverRepository.deleteAll();
        memberRepository.deleteAll();
        vehicleOptionRepository.deleteAll();
    }

    @Test
    void registerDriver_success() throws Exception {
        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", carModelId,
                                "carColorId", carColorId,
                                "carNumber", "12가3456"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.carNumber").value("12가3456"))
                .andExpect(jsonPath("$.data.carModelName").value("아반떼"))
                .andExpect(jsonPath("$.data.carColorName").value("흰색"));
    }

    @Test
    void registerDriver_alreadyRegistered_returns409() throws Exception {
        registerDriver("12가3456");

        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", carModelId,
                                "carColorId", carColorId,
                                "carNumber", "99나9999"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DRIVER_001"));
    }

    @Test
    void getMyDriver_success() throws Exception {
        registerDriver("12가3456");

        mockMvc.perform(get("/api/v1/drivers/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.carNumber").value("12가3456"))
                .andExpect(jsonPath("$.data.carModelName").value("아반떼"));
    }

    @Test
    void getMyDriver_notRegistered_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/drivers/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DRIVER_005"));
    }

    @Test
    void updateDriver_success() throws Exception {
        registerDriver("12가3456");

        VehicleOption newModel = vehicleOptionRepository.save(VehicleOption.builder()
                .type(VehicleOptionType.MODEL).brand("기아").name("K5").build());

        mockMvc.perform(put("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", newModel.getId(),
                                "carColorId", carColorId,
                                "carNumber", "99나9999"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.carNumber").value("99나9999"))
                .andExpect(jsonPath("$.data.carModelName").value("K5"));
    }

    @Test
    void updateDriver_notRegistered_returns404() throws Exception {
        mockMvc.perform(put("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", carModelId,
                                "carColorId", carColorId,
                                "carNumber", "12가3456"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DRIVER_005"));
    }

    @Test
    void deleteDriver_success() throws Exception {
        registerDriver("12가3456");

        mockMvc.perform(delete("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/drivers/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDriver_notRegistered_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DRIVER_005"));
    }

    @Test
    void registerDriver_carNumberDuplicate_returns409() throws Exception {
        registerDriver("12가3456");

        memberRepository.save(Member.builder()
                .email("driver2@test.com")
                .password(passwordEncoder.encode("password123"))
                .nickname("테스트드라이버2")
                .build());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "driver2@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String secondToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .at("/data/accessToken").asText();

        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", carModelId,
                                "carColorId", carColorId,
                                "carNumber", "12가3456"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DRIVER_004"));
    }

    @Test
    void registerDriver_invalidCarModelId_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", 9999L,
                                "carColorId", carColorId,
                                "carNumber", "12가3456"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DRIVER_002"));
    }

    @Test
    void registerDriver_invalidCarColorId_returns404() throws Exception {
        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", carModelId,
                                "carColorId", 9999L,
                                "carNumber", "12가3456"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DRIVER_003"));
    }

    @Test
    void registerDriver_missingCarNumber_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", carModelId,
                                "carColorId", carColorId
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }

    @Test
    void registerDriver_unauthorized_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", carModelId,
                                "carColorId", carColorId,
                                "carNumber", "12가3456"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    private void registerDriver(String carNumber) throws Exception {
        mockMvc.perform(post("/api/v1/drivers")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "carModelId", carModelId,
                                "carColorId", carColorId,
                                "carNumber", carNumber
                        ))))
                .andExpect(status().isCreated());
    }
}
