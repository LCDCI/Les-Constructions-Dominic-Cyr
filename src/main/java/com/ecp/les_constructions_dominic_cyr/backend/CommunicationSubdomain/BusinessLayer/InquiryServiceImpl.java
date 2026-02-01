package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.InquiryRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer.InquiryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InquiryServiceImpl implements InquiryService {
    private static final Logger log = LoggerFactory.getLogger(InquiryServiceImpl.class);
    
    private final InquiryRepository repository;
    private final InquiryMapper mapper;
    private final NotificationService notificationService;
    private final UsersRepository usersRepository;
    private final MailerServiceClient mailerServiceClient;

    public InquiryServiceImpl(
            InquiryRepository repository,
            InquiryMapper mapper,
            NotificationService notificationService,
            UsersRepository usersRepository,
            MailerServiceClient mailerServiceClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.usersRepository = usersRepository;
        this.mailerServiceClient = mailerServiceClient;
    }

    @Override
    @Transactional
    public InquiryResponseModel submitInquiry(InquiryRequestModel request) {
        // Save the inquiry
        Inquiry inquiry = mapper.requestModelToEntity(request);
        Inquiry savedInquiry = repository.save(inquiry);
        
        log.info("New inquiry submitted: {} from {}", request.getName(), request.getEmail());
        
        // Find owner user(s) and send notification + email
        List<Users> owners = usersRepository.findAll().stream()
                .filter(user -> user.getUserRole() == UserRole.OWNER)
                .collect(Collectors.toList());
        
        if (owners.isEmpty()) {
            log.warn("No owner user found. Notification and email will not be sent.");
        } else {
            // Send notification and email to all owners
            for (Users owner : owners) {
                try {
                    // Create in-app notification
                    String notificationTitle = "New Inquiry Received";
                    StringBuilder notificationMessageBuilder = new StringBuilder();
                    notificationMessageBuilder.append("A new inquiry has been received from ")
                            .append(request.getName())
                            .append(" (").append(request.getEmail()).append(")");
                    if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                        notificationMessageBuilder.append(". Phone: ").append(request.getPhone());
                    }
                    String notificationMessage = notificationMessageBuilder.toString();
                    
                    notificationService.createNotification(
                        owner.getUserIdentifier().getUserId(),
                        notificationTitle,
                        notificationMessage,
                        NotificationCategory.INQUIRY_RECEIVED,
                        "/inquiries" // Link to inquiries page
                    );
                    
                    log.info("Notification created for owner: {}", owner.getPrimaryEmail());
                    
                    // Send email to owner
                    String emailSubject = "New Inquiry from " + request.getName();
                    String emailBody = buildInquiryEmailBody(request);
                    
                    log.info("Attempting to send email to owner: {} via mailer service", owner.getPrimaryEmail());
                    
                    mailerServiceClient.sendEmail(
                        owner.getPrimaryEmail(),
                        emailSubject,
                        emailBody,
                        "Les Constructions Dominic Cyr"
                    ).subscribe(
                        null,
                        error -> {
                            log.error("Failed to send email to owner {}: {}", 
                                owner.getPrimaryEmail(), error.getMessage(), error);
                        },
                        () -> log.info("Email send request completed for owner: {}", owner.getPrimaryEmail())
                    );
                    
                } catch (Exception e) {
                    log.error("Error sending notification/email to owner {}: {}", 
                        owner.getPrimaryEmail(), e.getMessage(), e);
                    // Continue processing even if notification/email fails
                }
            }
        }
        
        return mapper.entityToResponseModel(savedInquiry);
    }
    
    private String buildInquiryEmailBody(InquiryRequestModel request) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }");
        html.append(".content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; }");
        html.append(".field { margin-bottom: 15px; }");
        html.append(".label { font-weight: bold; color: #555; }");
        html.append(".value { margin-top: 5px; padding: 10px; background-color: white; border-left: 3px solid #2c3e50; }");
        html.append(".message-box { margin-top: 20px; padding: 15px; background-color: white; border-left: 3px solid #3498db; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><h1>New Inquiry Received</h1></div>");
        html.append("<div class='content'>");
        html.append("<div class='field'><span class='label'>Name:</span><div class='value'>").append(escapeHtml(request.getName())).append("</div></div>");
        html.append("<div class='field'><span class='label'>Email:</span><div class='value'>").append(escapeHtml(request.getEmail())).append("</div></div>");
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            html.append("<div class='field'><span class='label'>Phone:</span><div class='value'>").append(escapeHtml(request.getPhone())).append("</div></div>");
        }
        html.append("<div class='message-box'><span class='label'>Message:</span><div class='value' style='white-space: pre-wrap;'>").append(escapeHtml(request.getMessage())).append("</div></div>");
        html.append("</div></div>");
        html.append("</body></html>");
        return html.toString();
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    @Override
    public List<InquiryResponseModel> getAllInquiries() {
        return repository.findAll()
                .stream()
                .map(mapper::entityToResponseModel)
                .collect(Collectors.toList());
    }
}
