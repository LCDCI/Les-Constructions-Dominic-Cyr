package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inquiries")
public class InquiryController {
    private final InquiryService service;

    public InquiryController(InquiryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> submit(@Valid @RequestBody InquiryRequestModel request) {
        service.submitInquiry(request);
        return ResponseEntity.ok(Map.of("message", "Thank you! Your inquiry has been received."));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<?> getAllInquiries() {
        List<InquiryResponseModel> inquiries = service.getAllInquiries();
        return ResponseEntity.ok(inquiries);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteInquiry(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    public ResponseEntity<InquiryResponseModel> setArchived(
            @PathVariable Long id,
            @RequestParam boolean archived) {
        return ResponseEntity.ok(service.setArchived(id, archived));
    }
}
