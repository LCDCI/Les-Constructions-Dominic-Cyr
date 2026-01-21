package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/inquiries")
@CrossOrigin(origins = "http://localhost:3000")
public class InquiryController {
    private final InquiryService service;

    public InquiryController(InquiryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> submit(@Valid @RequestBody InquiryRequestModel request) {
        // Input sanitization
        request.setName(HtmlUtils.htmlEscape(request.getName()));
        request.setEmail(HtmlUtils.htmlEscape(request.getEmail()));
        if (request.getPhone() != null) {
            request.setPhone(HtmlUtils.htmlEscape(request.getPhone()));
        }
        request.setMessage(HtmlUtils.htmlEscape(request.getMessage()));
        
        service.submitInquiry(request);
        return ResponseEntity.ok(Map.of("message", "Thank you! Your inquiry has been received."));
    }

    @GetMapping
    public ResponseEntity<?> getAllInquiries() {
        List<InquiryResponseModel> inquiries = service.getAllInquiries();
        return ResponseEntity.ok(inquiries);
    }
}
