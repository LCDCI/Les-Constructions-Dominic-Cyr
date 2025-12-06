package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DTOs;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Entities.Inquiry;

import java.time.OffsetDateTime;

public class InquiryResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final String phone;
    private final String message;
    private final OffsetDateTime createdAt;

    public InquiryResponse(Long id, String name, String email, String phone, String message, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.message = message;
        this.createdAt = createdAt;
    }

    public static InquiryResponse from(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getName(),
                inquiry.getEmail(),
                inquiry.getPhone(),
                inquiry.getMessage(),
                inquiry.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
