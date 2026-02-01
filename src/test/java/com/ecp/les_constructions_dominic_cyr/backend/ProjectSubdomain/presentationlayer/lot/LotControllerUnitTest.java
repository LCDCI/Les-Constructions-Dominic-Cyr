package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer.lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotController;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotService;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = LotController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LotControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LotService lotService;

    @MockBean
    private UserService userService;

    @MockBean
    private LotRepository lotRepository;

    @Test
    void getAllLotsByProject_AsOwner_ReturnsOk() throws Exception {
        when(lotService.getAllLotsByProject(anyString())).thenReturn(List.of(new LotResponseModel()));

        mockMvc.perform(get("/api/v1/projects/proj-123/lots"))
                .andExpect(status().isOk());
    }

    @Test
    void getLotById_WithValidUuid_ReturnsOk() throws Exception {
        String uuid = java.util.UUID.randomUUID().toString();
        when(lotService.getLotById(uuid)).thenReturn(new LotResponseModel());

        mockMvc.perform(get("/api/v1/projects/proj-123/lots/" + uuid))
                .andExpect(status().isOk());
    }
}
