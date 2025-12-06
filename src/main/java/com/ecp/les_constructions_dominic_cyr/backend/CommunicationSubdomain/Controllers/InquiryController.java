package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Controllers;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DTOs.InquiryRequest;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DTOs.InquiryResponse;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Services.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {
    private final InquiryService service;

    public InquiryController(InquiryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> submit(@Valid @RequestBody InquiryRequest request) {
        service.submitInquiry(request);
        return ResponseEntity.ok().body("Thank you! Your inquiry has been received.");
    }

    @GetMapping
    public ResponseEntity<List<InquiryResponse>> getAll() {
        List<InquiryResponse> inquiries = service.getInquiries()
                .stream()
                .map(InquiryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(inquiries);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> rejectDeletion() {
        return ResponseEntity.status(403).body("Inquiries cannot be deleted.");
    }
}
