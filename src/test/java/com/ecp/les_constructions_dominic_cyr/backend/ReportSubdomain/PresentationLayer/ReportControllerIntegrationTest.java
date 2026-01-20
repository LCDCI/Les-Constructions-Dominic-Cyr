package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer.GoogleAnalyticsService;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer.PDFReportGenerator;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer.XLSXReportGenerator;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReportRepository;
import com.ecp.les_constructions_dominic_cyr.backend.config.TestcontainersPostgresConfig;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalyticsReportRepository reportRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GoogleAnalyticsService googleAnalyticsService;

    @MockitoBean
    private PDFReportGenerator pdfReportGenerator;

    @MockitoBean
    private XLSXReportGenerator xlsxReportGenerator;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private Auth0ManagementService auth0ManagementService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final String BASE_URI = "/api/v1/reports";
    private final String USER_ID = "auth0|test-user-123";
    private final SimpleGrantedAuthority OWNER_ROLE = new SimpleGrantedAuthority("ROLE_OWNER");

    @BeforeEach
    void setUp() throws Exception {
        reportRepository.deleteAll();

        Map<String, Object> mockAnalytics = new HashMap<>();
        mockAnalytics.put("summary", Map.of("totalUsers", 100));
        mockAnalytics.put("dailyMetrics", Map.of("2023-01-01", Map.of()));

        when(googleAnalyticsService.fetchAnalyticsData(any(), any(), any())).thenReturn(mockAnalytics);
        when(pdfReportGenerator.generatePDFReport(any(), any(), any())).thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

        Map<String, Object> storageResponse = new HashMap<>();
        storageResponse.put("objectKey", "storage-uuid-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(storageResponse, HttpStatus.OK));
    }

    @Test
    void whenGenerateReport_thenReturnCreated() throws Exception {
        ReportRequestDTO req = new ReportRequestDTO();
        req.setReportType("AUDIT");
        req.setFileFormat("PDF");
        req.setStartDate(LocalDateTime.now().minusDays(7));
        req.setEndDate(LocalDateTime.now());

        mockMvc.perform(post(BASE_URI + "/generate")
                        .with(jwt().authorities(OWNER_ROLE).jwt(j -> j.subject(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportType").value("AUDIT"))
                .andExpect(jsonPath("$.fileFormat").value("PDF"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }


    @Test
    void whenInvalidGenerate_thenReturnBadRequest() throws Exception {
        ReportRequestDTO invalidReq = new ReportRequestDTO();

        mockMvc.perform(post(BASE_URI + "/generate")
                        .with(jwt().authorities(OWNER_ROLE).jwt(j -> j.subject(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }
}