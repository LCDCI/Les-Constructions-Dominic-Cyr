package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.House;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.House.HouseRepository;
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
class HouseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HouseRepository houseRepository;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final String BASE_URI = "/api/v1/houses";
    private final SimpleGrantedAuthority OWNER_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");

    @BeforeEach
    void setUp() {
        houseRepository.deleteAll();
    }

    //@Test
    void whenGetByIdExists_thenReturn() throws Exception {
        String idVal = UUID.randomUUID().toString();
        House entity = new House();
        entity.setHouseIdentifier(new HouseIdentifier(idVal));
        entity.setHouseName("ById House");
        entity.setLocation("ById Loc");
        entity.setDescription("ById Desc");
        entity.setNumberOfRooms(4);
        entity.setNumberOfBedrooms(2);
        entity.setNumberOfBathrooms(1);
        entity.setConstructionYear(2019);
        houseRepository.save(entity);

        mockMvc.perform(get(BASE_URI + "/" + idVal)
                        .with(jwt().authorities(OWNER_ROLE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houseId").value(idVal))
                .andExpect(jsonPath("$.houseName").value("ById House"));
    }

    //@Test
    void whenGetByIdNotExists_thenReturnNotFound() throws Exception {
        mockMvc.perform(get(BASE_URI + "/" + UUID.randomUUID())
                        .with(jwt().authorities(OWNER_ROLE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}