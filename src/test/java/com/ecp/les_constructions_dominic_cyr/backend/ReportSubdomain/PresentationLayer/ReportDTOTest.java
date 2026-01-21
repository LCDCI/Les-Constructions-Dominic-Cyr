package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.PresentationLayer;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ReportDTOTest {

    @Test
    void testReportRequestDTO_GettersSetters() {
        ReportRequestDTO dto = new ReportRequestDTO();
        LocalDateTime now = LocalDateTime.now();

        dto.setReportType("AUDIT");
        dto.setFileFormat("PDF");
        dto.setStartDate(now);
        dto.setEndDate(now);

        assertEquals("AUDIT", dto.getReportType());
        assertEquals("PDF", dto.getFileFormat());
        assertEquals(now, dto.getStartDate());
        assertEquals(now, dto.getEndDate());
    }

    @Test
    void testReportResponseDTO_GettersSetters() {
        ReportResponseDTO dto = new ReportResponseDTO();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        dto.setId(id);
        dto.setOwnerId("user-1");
        dto.setReportType("AUDIT");
        dto.setFileFormat("PDF");
        dto.setDownloadUrl("http://storage.com/file");
        dto.setFileSize(1024L);
        dto.setGenerationTimestamp(now);
        dto.setStartDate(now);
        dto.setEndDate(now);
        dto.setStatus("COMPLETED");
        dto.setMetadata(new HashMap<>());

        assertEquals(id, dto.getId());
        assertEquals("user-1", dto.getOwnerId());
        assertEquals("AUDIT", dto.getReportType());
        assertEquals("PDF", dto.getFileFormat());
        assertEquals("http://storage.com/file", dto.getDownloadUrl());
        assertEquals(1024L, dto.getFileSize());
        assertEquals(now, dto.getGenerationTimestamp());
        assertEquals(now, dto.getStartDate());
        assertEquals(now, dto.getEndDate());
        assertEquals("COMPLETED", dto.getStatus());
        assertNotNull(dto.getMetadata());
    }
}