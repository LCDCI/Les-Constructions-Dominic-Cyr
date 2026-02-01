package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer.lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LotController.class)
@AutoConfigureMockMvc
public class LotControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LotService lotService;

    @MockBean
    private UserService userService;

    @MockBean
    private LotRepository lotRepository;

    @Test
    void getAllLotsByProject_ReturnsOk() throws Exception {
        when(lotService.getAllLotsByProject(anyString())).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/projects/proj-xyz/lots"))
                .andExpect(status().isOk());
    }
}
