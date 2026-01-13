package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.*;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class LotControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final SimpleGrantedAuthority ADMIN_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");
    private final String BASE_URI = "/api/v1/lots";

    @BeforeEach
    void setup() {
        lotRepository.deleteAll();
    }

    @Test
    void whenGetAll_thenReturnList() throws Exception {
        LotIdentifier id = new LotIdentifier(UUID.randomUUID().toString());
        Lot entity = new Lot(id, "Integration Loc", 777f, "77x77", LotStatus.AVAILABLE);
        lotRepository.save(entity);

        mockMvc.perform(get(BASE_URI)
                        .with(jwt().authorities(ADMIN_ROLE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void whenGetByIdExists_thenReturn() throws Exception {
        String idVal = UUID.randomUUID().toString();
        lotRepository.save(new Lot(new LotIdentifier(idVal), "ById Loc", 111f, "11x11", LotStatus.AVAILABLE));

        mockMvc.perform(get(BASE_URI + "/{id}", idVal)
                        .with(jwt().authorities(ADMIN_ROLE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotId").value(idVal))
                .andExpect(jsonPath("$.location").value("ById Loc"));
    }


    @Test
    void whenDeleteExists_thenNoContent() throws Exception {
        String idVal = UUID.randomUUID().toString();
        lotRepository.save(new Lot(new LotIdentifier(idVal), "ToDelete", 11f, "11x11", LotStatus.AVAILABLE));

        mockMvc.perform(delete(BASE_URI + "/{id}", idVal)
                        .with(jwt().authorities(ADMIN_ROLE)))
                .andExpect(status().isNoContent());

        assertNull(lotRepository.findByLotIdentifier_LotId(idVal));
    }
}