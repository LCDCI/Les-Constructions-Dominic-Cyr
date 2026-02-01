package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.businesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.InquiryServiceImpl;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.MailerServiceClient;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.BusinessLayer.NotificationService;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.InquiryRepository;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.MapperLayer.InquiryMapper;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.PresentationLayer.InquiryResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserRole;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserStatus;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryServiceImplTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryMapper inquiryMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private MailerServiceClient mailerServiceClient;

    @InjectMocks
    private InquiryServiceImpl inquiryService;

    private InquiryRequestModel testRequest;
    private Inquiry testInquiry;
    private Inquiry savedInquiry;
    private InquiryResponseModel testResponse;
    private Users ownerUser1;
    private Users ownerUser2;
    private Users nonOwnerUser;

    @BeforeEach
    void setUp() {
        // Setup test request
        testRequest = new InquiryRequestModel();
        testRequest.setName("John Doe");
        testRequest.setEmail("john@example.com");
        testRequest.setPhone("555-1234");
        testRequest.setMessage("I'm interested in your services.");

        // Setup test inquiry
        testInquiry = new Inquiry();
        testInquiry.setName("John Doe");
        testInquiry.setEmail("john@example.com");
        testInquiry.setPhone("555-1234");
        testInquiry.setMessage("I'm interested in your services.");

        // Setup saved inquiry
        savedInquiry = new Inquiry();
        savedInquiry.setId(1L);
        savedInquiry.setName("John Doe");
        savedInquiry.setEmail("john@example.com");
        savedInquiry.setPhone("555-1234");
        savedInquiry.setMessage("I'm interested in your services.");
        savedInquiry.setCreatedAt(OffsetDateTime.now());

        // Setup response model
        testResponse = new InquiryResponseModel();
        testResponse.setId(1L);
        testResponse.setName("John Doe");
        testResponse.setEmail("john@example.com");
        testResponse.setPhone("555-1234");
        testResponse.setMessage("I'm interested in your services.");
        testResponse.setCreatedAt(OffsetDateTime.now());

        // Setup owner users
        UserIdentifier ownerId1 = UserIdentifier.newId();
        ownerUser1 = new Users(
                ownerId1,
                "Owner",
                "One",
                "owner1@example.com",
                null,
                "555-0001",
                UserRole.OWNER,
                "auth0|owner1",
                UserStatus.ACTIVE
        );

        UserIdentifier ownerId2 = UserIdentifier.newId();
        ownerUser2 = new Users(
                ownerId2,
                "Owner",
                "Two",
                "owner2@example.com",
                null,
                "555-0002",
                UserRole.OWNER,
                "auth0|owner2",
                UserStatus.ACTIVE
        );

        // Setup non-owner user
        UserIdentifier nonOwnerId = UserIdentifier.newId();
        nonOwnerUser = new Users(
                nonOwnerId,
                "Sales",
                "Person",
                "sales@example.com",
                null,
                "555-0003",
                UserRole.SALESPERSON,
                "auth0|sales",
                UserStatus.ACTIVE
        );
    }

    @Test
    void submitInquiry_WithOwnerUsers_CreatesNotificationAndSendsEmail() {
        // Arrange
        List<Users> allUsers = Arrays.asList(ownerUser1, nonOwnerUser);
        
        when(inquiryMapper.requestModelToEntity(testRequest)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponse);
        when(usersRepository.findAll()).thenReturn(allUsers);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());

        // Verify notification was created for owner
        verify(notificationService, times(1)).createNotification(
                eq(ownerUser1.getUserIdentifier().getUserId()),
                eq("New Inquiry Received"),
                anyString(),
                eq(com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory.INQUIRY_RECEIVED),
                eq("/inquiries")
        );

        // Verify email was sent to owner
        verify(mailerServiceClient, times(1)).sendEmail(
                eq("owner1@example.com"),
                eq("New Inquiry from John Doe"),
                anyString(),
                eq("Les Constructions Dominic Cyr")
        );

        // Verify non-owner did not receive notification/email
        verify(notificationService, never()).createNotification(
                eq(nonOwnerUser.getUserIdentifier().getUserId()),
                anyString(),
                anyString(),
                any(),
                anyString()
        );
    }

    @Test
    void submitInquiry_WithMultipleOwners_NotifiesAllOwners() {
        // Arrange
        List<Users> allUsers = Arrays.asList(ownerUser1, ownerUser2, nonOwnerUser);
        
        when(inquiryMapper.requestModelToEntity(testRequest)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponse);
        when(usersRepository.findAll()).thenReturn(allUsers);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequest);

        // Assert
        assertNotNull(result);

        // Verify notifications created for both owners
        verify(notificationService, times(2)).createNotification(
                any(UUID.class),
                eq("New Inquiry Received"),
                anyString(),
                eq(com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationCategory.INQUIRY_RECEIVED),
                eq("/inquiries")
        );

        // Verify emails sent to both owners
        verify(mailerServiceClient, times(2)).sendEmail(
                anyString(),
                eq("New Inquiry from John Doe"),
                anyString(),
                eq("Les Constructions Dominic Cyr")
        );
    }

    @Test
    void submitInquiry_WithNoOwners_DoesNotSendNotificationsOrEmails() {
        // Arrange
        List<Users> allUsers = Arrays.asList(nonOwnerUser);
        
        when(inquiryMapper.requestModelToEntity(testRequest)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponse);
        when(usersRepository.findAll()).thenReturn(allUsers);

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());

        // Verify no notifications or emails were sent
        verify(notificationService, never()).createNotification(
                any(UUID.class),
                anyString(),
                anyString(),
                any(),
                anyString()
        );
        verify(mailerServiceClient, never()).sendEmail(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void submitInquiry_WithNullPhone_HandlesGracefully() {
        // Arrange
        testRequest.setPhone(null);
        testInquiry.setPhone(null);
        savedInquiry.setPhone(null);
        testResponse.setPhone(null);

        List<Users> allUsers = Arrays.asList(ownerUser1);
        
        when(inquiryMapper.requestModelToEntity(testRequest)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponse);
        when(usersRepository.findAll()).thenReturn(allUsers);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequest);

        // Assert
        assertNotNull(result);
        assertNull(result.getPhone());

        // Verify notification message doesn't include phone
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).createNotification(
                any(UUID.class),
                anyString(),
                messageCaptor.capture(),
                any(),
                anyString()
        );
        String notificationMessage = messageCaptor.getValue();
        assertFalse(notificationMessage.contains("Phone:"));
    }

    @Test
    void submitInquiry_WithEmptyPhone_HandlesGracefully() {
        // Arrange
        testRequest.setPhone("");
        List<Users> allUsers = Arrays.asList(ownerUser1);
        
        when(inquiryMapper.requestModelToEntity(testRequest)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponse);
        when(usersRepository.findAll()).thenReturn(allUsers);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequest);

        // Assert
        assertNotNull(result);

        // Verify notification message doesn't include phone
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).createNotification(
                any(UUID.class),
                anyString(),
                messageCaptor.capture(),
                any(),
                anyString()
        );
        String notificationMessage = messageCaptor.getValue();
        assertFalse(notificationMessage.contains("Phone:"));
    }

    @Test
    void submitInquiry_WithEmailError_ContinuesProcessing() {
        // Arrange
        List<Users> allUsers = Arrays.asList(ownerUser1);
        
        when(inquiryMapper.requestModelToEntity(testRequest)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponse);
        when(usersRepository.findAll()).thenReturn(allUsers);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Email service error")));

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequest);

        // Assert - Should still return result even if email fails
        assertNotNull(result);
        assertEquals(1L, result.getId());

        // Verify notification was still created
        verify(notificationService, times(1)).createNotification(
                any(UUID.class),
                anyString(),
                anyString(),
                any(),
                anyString()
        );
    }

    @Test
    void submitInquiry_WithNotificationError_ContinuesProcessing() {
        // Arrange
        List<Users> allUsers = Arrays.asList(ownerUser1);
        
        when(inquiryMapper.requestModelToEntity(testRequest)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponse);
        when(usersRepository.findAll()).thenReturn(allUsers);
        doThrow(new RuntimeException("Notification error"))
                .when(notificationService).createNotification(
                        any(UUID.class),
                        anyString(),
                        anyString(),
                        any(),
                        anyString());
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        InquiryResponseModel result = inquiryService.submitInquiry(testRequest);

        // Assert - Should still return result even if notification fails
        assertNotNull(result);
        assertEquals(1L, result.getId());

        // Verify email was still attempted
        verify(mailerServiceClient, times(1)).sendEmail(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void submitInquiry_EmailBodyContainsEscapedHtml() {
        // Arrange
        testRequest.setMessage("Test <script>alert('xss')</script> & special chars");
        List<Users> allUsers = Arrays.asList(ownerUser1);
        
        when(inquiryMapper.requestModelToEntity(testRequest)).thenReturn(testInquiry);
        when(inquiryRepository.save(testInquiry)).thenReturn(savedInquiry);
        when(inquiryMapper.entityToResponseModel(savedInquiry)).thenReturn(testResponse);
        when(usersRepository.findAll()).thenReturn(allUsers);
        when(mailerServiceClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Mono.empty());

        // Act
        inquiryService.submitInquiry(testRequest);

        // Assert - Verify email body contains escaped HTML
        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailerServiceClient).sendEmail(
                anyString(),
                anyString(),
                emailBodyCaptor.capture(),
                anyString()
        );
        String emailBody = emailBodyCaptor.getValue();
        assertTrue(emailBody.contains("&lt;script&gt;"));
        assertTrue(emailBody.contains("&amp;"));
        assertTrue(emailBody.contains("&gt;"));
    }

    @Test
    void getAllInquiries_ReturnsAllInquiries() {
        // Arrange
        Inquiry inquiry1 = new Inquiry();
        inquiry1.setId(1L);
        inquiry1.setName("John Doe");
        inquiry1.setEmail("john@example.com");

        Inquiry inquiry2 = new Inquiry();
        inquiry2.setId(2L);
        inquiry2.setName("Jane Smith");
        inquiry2.setEmail("jane@example.com");

        InquiryResponseModel response1 = new InquiryResponseModel();
        response1.setId(1L);
        response1.setName("John Doe");
        response1.setEmail("john@example.com");

        InquiryResponseModel response2 = new InquiryResponseModel();
        response2.setId(2L);
        response2.setName("Jane Smith");
        response2.setEmail("jane@example.com");

        when(inquiryRepository.findAll()).thenReturn(Arrays.asList(inquiry1, inquiry2));
        when(inquiryMapper.entityToResponseModel(inquiry1)).thenReturn(response1);
        when(inquiryMapper.entityToResponseModel(inquiry2)).thenReturn(response2);

        // Act
        List<InquiryResponseModel> result = inquiryService.getAllInquiries();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());

        verify(inquiryRepository, times(1)).findAll();
        verify(inquiryMapper, times(2)).entityToResponseModel(any(Inquiry.class));
    }

    @Test
    void getAllInquiries_WithEmptyRepository_ReturnsEmptyList() {
        // Arrange
        when(inquiryRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<InquiryResponseModel> result = inquiryService.getAllInquiries();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inquiryRepository, times(1)).findAll();
        verify(inquiryMapper, never()).entityToResponseModel(any(Inquiry.class));
    }
}
