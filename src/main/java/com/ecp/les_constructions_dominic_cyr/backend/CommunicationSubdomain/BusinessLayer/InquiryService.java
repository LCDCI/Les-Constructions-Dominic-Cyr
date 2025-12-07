package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer;

// Define BusinessLayer DTOs for inquiry input and output
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryDTO;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryResultDTO;

public interface InquiryService {
    InquiryResultDTO submitInquiry(InquiryDTO inquiry);
}

// DTOs for BusinessLayer (should be moved to their own files in a real project)
public class InquiryDTO {
    // Add fields relevant to the inquiry, e.g.:
    // private String name;
    // private String email;
    // private String message;
    // Add constructors, getters, setters as needed
}

public class InquiryResultDTO {
    // Add fields relevant to the result, e.g.:
    // private boolean success;
    // private String responseMessage;
    // Add constructors, getters, setters as needed
}
