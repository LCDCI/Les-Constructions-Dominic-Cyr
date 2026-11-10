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
import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.NotFoundException;

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
                    String notificationTitle = "Nouvelle demande reçue";
                    StringBuilder notificationMessageBuilder = new StringBuilder();
                    notificationMessageBuilder.append("Une nouvelle demande a été reçue de ")
                            .append(request.getName())
                            .append(" (").append(request.getEmail()).append(")");
                    if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                        notificationMessageBuilder.append(". Téléphone : ").append(request.getPhone());
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
                    String emailSubject = "Nouvelle demande de " + request.getName();
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

    @Override
    @Transactional
    public void deleteInquiry(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Inquiry not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public InquiryResponseModel setArchived(Long id, boolean archived) {
        Inquiry inquiry = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inquiry not found: " + id));
        inquiry.setArchived(archived);
        return mapper.entityToResponseModel(repository.save(inquiry));
    }
    
    private String buildInquiryEmailBody(InquiryRequestModel request) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { margin: 0; padding: 24px 12px; background: #f5f5f5; font-family: Arial, sans-serif; line-height: 1.6; color: #1a1a1a; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden; }");
        html.append(".header { background: #2c3531; color: #fff; padding: 28px 24px; text-align: center; }");
        html.append(".logo { display: block; width: 220px; max-width: 80%; height: auto; margin: 0 auto 18px; background: #fff; border-radius: 4px; padding: 8px; }");
        html.append(".header h1 { margin: 0; font-size: 24px; }");
        html.append(".content { background: #fafbfc; padding: 28px 24px; }");
        html.append(".field { margin-bottom: 18px; }");
        html.append(".label { display: block; margin-bottom: 6px; font-weight: bold; color: #6a7266; }");
        html.append(".value { padding: 12px 14px; background: #fff; border: 1px solid #e5e7eb; border-left: 4px solid #aab2a6; border-radius: 4px; }");
        html.append(".message-box { margin-top: 24px; padding: 16px; background: #f5f5f5; border-left: 4px solid #aab2a6; border-radius: 4px; }");
        html.append(".footer { padding: 16px 24px; text-align: center; color: #6b7280; font-size: 12px; background: #fff; border-top: 1px solid #e5e7eb; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><img class='logo' src='https://constructions-dominiccyr.com/fallback.jpg' alt='Les Constructions Dominic Cyr'><h1>Nouvelle demande reçue</h1></div>");
        html.append("<div class='content'>");
        html.append("<div class='field'><span class='label'>Nom :</span><div class='value'>").append(escapeHtml(request.getName())).append("</div></div>");
        html.append("<div class='field'><span class='label'>Courriel :</span><div class='value'>").append(escapeHtml(request.getEmail())).append("</div></div>");
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            html.append("<div class='field'><span class='label'>Téléphone :</span><div class='value'>").append(escapeHtml(request.getPhone())).append("</div></div>");
        }
        html.append("<div class='message-box'><span class='label'>Message :</span><div class='value' style='white-space: pre-wrap;'>").append(escapeHtml(request.getMessage())).append("</div></div>");
        html.append("</div><div class='footer'>Les Constructions Dominic Cyr Inc.</div></div>");
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
