package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
