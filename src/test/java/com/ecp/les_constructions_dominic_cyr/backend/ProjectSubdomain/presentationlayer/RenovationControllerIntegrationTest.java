package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Renovation.*;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class RenovationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRepository renovationRepository;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final SimpleGrantedAuthority ADMIN_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");
    private final String BASE_URI = "/api/v1/renovations";

    @BeforeEach
    void setup() {
        renovationRepository.deleteAll();
    }

    //@Test
    void whenGetByIdExists_thenReturn() throws Exception {
        String idVal = UUID.randomUUID().toString();
        Renovation entity = new Renovation();
        entity.setRenovationIdentifier(new RenovationIdentifier(idVal));
        entity.setBeforeImageIdentifier("before-byid");
        entity.setAfterImageIdentifier("after-byid");
        entity.setDescription("ById Desc");
        renovationRepository.save(entity);

        mockMvc.perform(get(BASE_URI + "/{id}", idVal)
                        .with(jwt().authorities(ADMIN_ROLE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.renovationId").value(idVal))
                .andExpect(jsonPath("$.description").value("ById Desc"));
    }

    //@Test
    void whenGetByIdInvalidUUID_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URI + "/not-a-uuid")
                        .with(jwt().authorities(ADMIN_ROLE)))
                .andExpect(status().isBadRequest());
    }
}