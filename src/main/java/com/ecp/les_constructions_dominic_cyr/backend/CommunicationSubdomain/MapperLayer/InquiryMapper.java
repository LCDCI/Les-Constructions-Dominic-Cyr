package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import org.springframework.stereotype.Component;

@Component
public class InquiryMapper {
    
    public Inquiry requestModelToEntity(InquiryRequestModel requestModel) {
        Inquiry inquiry = new Inquiry();
        inquiry.setName(requestModel.getName());
        inquiry.setEmail(requestModel.getEmail());
        inquiry.setPhone(requestModel.getPhone());
        inquiry.setMessage(requestModel.getMessage());
        return inquiry;
    }
    
    public InquiryResponseModel entityToResponseModel(Inquiry inquiry) {
        InquiryResponseModel responseModel = new InquiryResponseModel();
        responseModel.setId(inquiry.getId());
        responseModel.setName(inquiry.getName());
        responseModel.setEmail(inquiry.getEmail());
        responseModel.setPhone(inquiry.getPhone());
        responseModel.setMessage(inquiry.getMessage());
        responseModel.setCreatedAt(inquiry.getCreatedAt());
        return responseModel;
    }
}
