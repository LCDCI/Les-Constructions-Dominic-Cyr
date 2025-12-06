package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Services;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DTOs.InquiryRequest;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Entities.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Repositories.InquiryRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InquiryService {
    private final InquiryRepository repository;

    public InquiryService(InquiryRepository repository) {
        this.repository = repository;
    }

    public Inquiry submitInquiry(InquiryRequest request) {
        Inquiry inquiry = new Inquiry();
        inquiry.setName(request.getName());
        inquiry.setEmail(request.getEmail());
        inquiry.setPhone(request.getPhone());
        inquiry.setMessage(request.getMessage());
        // createdAt is set automatically by @CreationTimestamp (Hibernate) in normal operation,
        // but may need to be set manually in certain cases (e.g., in tests or when importing legacy data)
        return repository.save(inquiry);
    }

    public List<Inquiry> getInquiries() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
