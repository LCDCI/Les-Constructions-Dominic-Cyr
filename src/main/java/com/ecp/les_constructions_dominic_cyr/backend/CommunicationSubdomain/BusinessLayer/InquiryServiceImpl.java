package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.InquiryRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer.InquiryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InquiryServiceImpl implements InquiryService {
    private final InquiryRepository repository;
    private final InquiryMapper mapper;

    public InquiryServiceImpl(InquiryRepository repository, InquiryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public InquiryResponseModel submitInquiry(InquiryRequestModel request) {
        Inquiry inquiry = mapper.requestModelToEntity(request);
        Inquiry savedInquiry = repository.save(inquiry);
        return mapper.entityToResponseModel(savedInquiry);
    }

    @Override
    public List<InquiryResponseModel> getAllInquiries() {
        return repository.findAll()
                .stream()
                .map(mapper::entityToResponseModel)
                .collect(Collectors.toList());
    }
}
