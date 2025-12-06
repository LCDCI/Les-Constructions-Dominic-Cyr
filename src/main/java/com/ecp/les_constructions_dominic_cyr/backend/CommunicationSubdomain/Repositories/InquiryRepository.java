package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Repositories;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.Entities.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByOrderByCreatedAtDesc();
}
