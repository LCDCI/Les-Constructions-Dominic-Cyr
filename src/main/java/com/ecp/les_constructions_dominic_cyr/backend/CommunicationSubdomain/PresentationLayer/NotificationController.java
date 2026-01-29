package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final UsersRepository usersRepository;

    public NotificationController(NotificationService notificationService, UsersRepository usersRepository) {
        this.notificationService = notificationService;
        this.usersRepository = usersRepository;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String auth0UserId = authentication.getName();
        Users user = usersRepository.findByAuth0UserId(auth0UserId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        return user.getUserIdentifier().getUserId();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseModel>> getAllNotifications() {
        UUID userId = getCurrentUserId();
        List<NotificationResponseModel> notifications = notificationService.getAllNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseModel>> getUnreadNotifications() {
        UUID userId = getCurrentUserId();
        List<NotificationResponseModel> notifications = notificationService.getUnreadNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        UUID userId = getCurrentUserId();
        Long count = notificationService.getUnreadCountByUserId(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable UUID notificationId) {
        UUID userId = getCurrentUserId();
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        UUID userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @PostMapping("/test/seed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> seedTestNotifications() {
        UUID userId = getCurrentUserId();
        
        // Create various test notifications
        notificationService.createNotification(
            userId,
            "New Task Assigned",
            "You have been assigned a new task: 'Foundation Inspection' for Project Foresta. Please review the details and update the status.",
            NotificationCategory.TASK_ASSIGNED,
            "/tasks/task-001"
        );

        notificationService.createNotification(
            userId,
            "Project Update Available",
            "Project 'Naturest' has been updated with new schedule information. The completion date has been moved to March 15, 2024.",
            NotificationCategory.PROJECT_UPDATED,
            "/projects/proj-002-naturest"
        );

        notificationService.createNotification(
            userId,
            "Form Submission Required",
            "A new form 'Safety Inspection Checklist' requires your attention. Please complete and submit by Friday.",
            NotificationCategory.FORM_SUBMITTED,
            "/forms/form-003"
        );

        notificationService.createNotification(
            userId,
            "Document Uploaded",
            "New document 'Building Plans v2.pdf' has been uploaded to Project Foresta. You can review it in the Documents section.",
            NotificationCategory.DOCUMENT_UPLOADED,
            "/projects/proj-001-foresta/files"
        );

        notificationService.createNotification(
            userId,
            "Schedule Created",
            "A new schedule has been created for the week of January 15-21. You have 3 tasks assigned.",
            NotificationCategory.SCHEDULE_CREATED,
            "/projects/proj-001-foresta/schedule"
        );

        notificationService.createNotification(
            userId,
            "Team Member Added",
            "John Smith has been added as a Contractor to Project Naturest. You can view team details in the project settings.",
            NotificationCategory.TEAM_MEMBER_ASSIGNED,
            "/projects/proj-002-naturest/team-management"
        );

        notificationService.createNotification(
            userId,
            "Task Completed",
            "Task 'Electrical Inspection' has been marked as completed by the assigned contractor. Please review the completion report.",
            NotificationCategory.TASK_COMPLETED,
            "/tasks/task-002"
        );

        notificationService.createNotification(
            userId,
            "Quote Submitted",
            "A new quote has been submitted for Project Foresta. Amount: $45,000. Please review and approve.",
            NotificationCategory.QUOTE_SUBMITTED,
            "/quotes/quote-001"
        );

        notificationService.createNotification(
            userId,
            "Form Signed",
            "The 'Contract Agreement' form has been signed by all parties. The document is now available in your documents.",
            NotificationCategory.FORM_SIGNED,
            "/documents"
        );

        notificationService.createNotification(
            userId,
            "Project Milestone Reached",
            "Project Naturest has reached 50% completion. Great progress! View the project dashboard for details.",
            NotificationCategory.PROJECT_UPDATED,
            "/projects/proj-002-naturest"
        );

        notificationService.createNotification(
            userId,
            "Inquiry Received",
            "A new customer inquiry has been received. Contact: Marie Dubois, Phone: (514) 555-0123. Please respond within 24 hours.",
            NotificationCategory.INQUIRY_RECEIVED,
            "/inquiries"
        );

        notificationService.createNotification(
            userId,
            "Schedule Updated",
            "The schedule for Project Foresta has been updated. Your task 'Roofing Installation' has been moved to next week.",
            NotificationCategory.SCHEDULE_UPDATED,
            "/projects/proj-001-foresta/schedule"
        );

        notificationService.createNotification(
            userId,
            "Welcome to the Portal",
            "Welcome to Les Constructions Dominic Cyr portal. You can manage your projects, tasks, and documents from here.",
            NotificationCategory.GENERAL,
            "/"
        );
        
        return ResponseEntity.ok(Map.of("message", "Test notifications created successfully", "count", "13"));
    }
}
