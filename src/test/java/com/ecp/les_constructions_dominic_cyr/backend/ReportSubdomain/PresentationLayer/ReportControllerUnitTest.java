package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer.ReportService;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.MapperLayer.ReportMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportController reportController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(Jwt.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return Jwt.withTokenValue("mock-token")
                                .header("alg", "none")
                                .subject("user-123")
                                .build();
                    }
                })
                .build();
    }

    @Test
    void generateReport_Success() throws Exception {
        ReportRequestDTO request = new ReportRequestDTO();
        request.setReportType("AUDIT");
        request.setFileFormat("PDF");

        AnalyticsReport report = new AnalyticsReport();
        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setReportType("AUDIT");

        when(reportService.generateReport(anyString(), anyString(), anyString(), any(), any())).thenReturn(report);
        when(reportMapper.toDTO(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/reports/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportType").value("AUDIT"));
    }

    @Test
    void getReports_Failure() throws Exception {
        Page<AnalyticsReport> page = new PageImpl<>(Collections.singletonList(new AnalyticsReport()));
        when(reportService.getReportsByOwner(anyString(), any(PageRequest.class))).thenReturn(page);
        when(reportMapper.toDTO(any())).thenReturn(new ReportResponseDTO());

        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void downloadReport_Pdf_Success() throws Exception {
        UUID reportId = UUID.randomUUID();
        AnalyticsReport report = new AnalyticsReport();
        report.setFileFormat("PDF");
        report.setGenerationTimestamp(LocalDateTime.now());

        when(reportService.getReportById(eq(reportId), anyString())).thenReturn(report);
        when(reportService.downloadReport(eq(reportId), anyString())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/v1/reports/{reportId}/download", reportId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void deleteReport_Success() throws Exception {
        UUID reportId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/reports/{reportId}", reportId))
                .andExpect(status().isNoContent());
    }
}