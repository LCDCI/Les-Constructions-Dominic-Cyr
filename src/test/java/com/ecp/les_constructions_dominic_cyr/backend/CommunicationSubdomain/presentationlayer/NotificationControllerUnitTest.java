package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.presentationlayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.NotificationController;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.NotificationResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UsersRepository usersRepository;

    private ObjectMapper objectMapper;
    private UUID testUserId;
    private UUID testNotificationId;
    private Users testUser;
    private NotificationResponseModel testNotification;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUserId = UUID.randomUUID();
        testNotificationId = UUID.randomUUID();
        String auth0UserId = "auth0|123456";

        UserIdentifier userIdentifier = UserIdentifier.fromString(testUserId.toString());
        testUser = new Users(
                userIdentifier,
                "John",
                "Doe",
                "john.doe@example.com",
                null,
                "555-1234",
                null,
                auth0UserId,
                null
        );

        testNotification = new NotificationResponseModel(
                testNotificationId,
                "Test Notification",
                "Test message",
                null,
                "/test/link",
                false,
                LocalDateTime.now()
        );

        // Setup mock authentication
        mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn(auth0UserId);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllNotifications_WithAuthenticatedUser_ReturnsNotifications() throws Exception {
        // Arrange
        List<NotificationResponseModel> notifications = Arrays.asList(testNotification);
        when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.of(testUser));
        when(notificationService.getAllNotificationsByUserId(testUserId)).thenReturn(notifications);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].notificationId").value(testNotificationId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Notification"));

        verify(notificationService, times(1)).getAllNotificationsByUserId(testUserId);
    }

    @Test
    void getAllNotifications_WithNoNotifications_ReturnsEmptyList() throws Exception {
        // Arrange
        when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.of(testUser));
        when(notificationService.getAllNotificationsByUserId(testUserId))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(notificationService, times(1)).getAllNotificationsByUserId(testUserId);
    }

    @Test
    void getUnreadNotifications_WithUnreadNotifications_ReturnsUnreadOnly() throws Exception {
        // Arrange
        NotificationResponseModel unreadNotification = new NotificationResponseModel(
                UUID.randomUUID(),
                "Unread Notification",
                "Unread message",
                null,
                null,
                false,
                LocalDateTime.now()
        );
        List<NotificationResponseModel> unreadNotifications = Arrays.asList(unreadNotification);

        when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.of(testUser));
        when(notificationService.getUnreadNotificationsByUserId(testUserId))
                .thenReturn(unreadNotifications);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isRead").value(false));

        verify(notificationService, times(1)).getUnreadNotificationsByUserId(testUserId);
    }

    @Test
    void getUnreadCount_WithUnreadNotifications_ReturnsCount() throws Exception {
        // Arrange
        when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.of(testUser));
        when(notificationService.getUnreadCountByUserId(testUserId)).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(5));

        verify(notificationService, times(1)).getUnreadCountByUserId(testUserId);
    }

    @Test
    void getUnreadCount_WithNoUnreadNotifications_ReturnsZero() throws Exception {
        // Arrange
        when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.of(testUser));
        when(notificationService.getUnreadCountByUserId(testUserId)).thenReturn(0L);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));

        verify(notificationService, times(1)).getUnreadCountByUserId(testUserId);
    }

    @Test
    void markAsRead_WithValidNotificationId_MarksAsRead() throws Exception {
        // Arrange
        when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(notificationService).markAsRead(testNotificationId, testUserId);

        // Act & Assert
        mockMvc.perform(put("/api/v1/notifications/{notificationId}/read", testNotificationId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Notification marked as read"));

        verify(notificationService, times(1)).markAsRead(testNotificationId, testUserId);
    }

    @Test
    void markAllAsRead_WithValidUser_MarksAllAsRead() throws Exception {
        // Arrange
        when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(notificationService).markAllAsRead(testUserId);

        // Act & Assert
        mockMvc.perform(put("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));

        verify(notificationService, times(1)).markAllAsRead(testUserId);
    }

    @Test
    void getAllNotifications_WithUserNotFound_ThrowsException() throws Exception {
        // Arrange
        when(usersRepository.findByAuth0UserId(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().is5xxServerError());

        verify(notificationService, never()).getAllNotificationsByUserId(any());
    }

    @Test
    void getAllNotifications_WithNullAuthentication_ThrowsException() throws Exception {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(null);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().is5xxServerError());

        verify(notificationService, never()).getAllNotificationsByUserId(any());
    }
}
