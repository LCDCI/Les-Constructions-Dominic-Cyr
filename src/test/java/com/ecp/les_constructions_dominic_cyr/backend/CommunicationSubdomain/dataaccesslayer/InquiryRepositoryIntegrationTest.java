package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.dataaccesslayer;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.Inquiry;
import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.InquiryRepository;
import com.ecp.les_constructions_dominic_cyr.backend.TestcontainersPostgresConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersPostgresConfig.class)
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE)
public class InquiryRepositoryIntegrationTest {

    @Autowired
    private InquiryRepository inquiryRepository;

    private Inquiry testInquiry;

    @BeforeEach
    void setUp() {
        inquiryRepository.deleteAll();

        testInquiry = new Inquiry();
        testInquiry.setName("John Doe");
        testInquiry.setEmail("john.doe@example.com");
        testInquiry.setPhone("555-1234");
        testInquiry.setMessage("I am interested in your construction services.");
    }

    @Test
    void save_WhenValidInquiry_SavesSuccessfully() {
        // Act
        Inquiry saved = inquiryRepository.save(testInquiry);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("John Doe", saved.getName());
        assertEquals("john.doe@example.com", saved.getEmail());
        assertEquals("555-1234", saved.getPhone());
        assertEquals("I am interested in your construction services.", saved.getMessage());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void save_WhenNullPhone_SavesSuccessfully() {
        // Arrange
        testInquiry.setPhone(null);

        // Act
        Inquiry saved = inquiryRepository.save(testInquiry);

        // Assert
        assertNotNull(saved.getId());
        assertNull(saved.getPhone());
        assertEquals("John Doe", saved.getName());
    }

    @Test
    void findById_WhenExists_ReturnsInquiry() {
        // Arrange
        Inquiry saved = inquiryRepository.save(testInquiry);

        // Act
        Optional<Inquiry> found = inquiryRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    void findById_WhenNotExists_ReturnsEmpty() {
        // Act
        Optional<Inquiry> found = inquiryRepository.findById(999L);

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_WhenMultipleInquiriesExist_ReturnsAllInquiries() {
        // Arrange
        Inquiry inquiry1 = new Inquiry();
        inquiry1.setName("Jane Smith");
        inquiry1.setEmail("jane.smith@example.com");
        inquiry1.setMessage("Looking for renovation services");
        
        Inquiry inquiry2 = new Inquiry();
        inquiry2.setName("Bob Johnson");
        inquiry2.setEmail("bob.johnson@example.com");
        inquiry2.setMessage("Need a quote for new construction");

        inquiryRepository.save(testInquiry);
        inquiryRepository.save(inquiry1);
        inquiryRepository.save(inquiry2);

        // Act
        List<Inquiry> inquiries = inquiryRepository.findAll();

        // Assert
        assertEquals(3, inquiries.size());
    }

    @Test
    void delete_WhenExists_DeletesSuccessfully() {
        // Arrange
        Inquiry saved = inquiryRepository.save(testInquiry);
        Long savedId = saved.getId();

        // Act
        inquiryRepository.deleteById(savedId);

        // Assert
        Optional<Inquiry> found = inquiryRepository.findById(savedId);
        assertTrue(found.isEmpty());
    }

    @Test
    void createdAt_WhenSaved_IsAutomaticallySet() {
        // Arrange
        OffsetDateTime beforeSave = OffsetDateTime.now();

        // Act
        Inquiry saved = inquiryRepository.save(testInquiry);

        // Assert
        OffsetDateTime afterSave = OffsetDateTime.now();
        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().isAfter(beforeSave.minusSeconds(1)));
        assertTrue(saved.getCreatedAt().isBefore(afterSave.plusSeconds(1)));
    }

    @Test
    void count_WhenInquiriesExist_ReturnsCorrectCount() {
        // Arrange
        inquiryRepository.save(testInquiry);
        
        Inquiry inquiry2 = new Inquiry();
        inquiry2.setName("Alice Brown");
        inquiry2.setEmail("alice.brown@example.com");
        inquiry2.setMessage("Question about services");
        inquiryRepository.save(inquiry2);

        // Act
        long count = inquiryRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void save_WhenLongMessage_SavesSuccessfully() {
        // Arrange
        String longMessage = "A".repeat(2000); // Max length
        testInquiry.setMessage(longMessage);

        // Act
        Inquiry saved = inquiryRepository.save(testInquiry);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(2000, saved.getMessage().length());
    }

    @Test
    void save_WhenMaxLengthFields_SavesSuccessfully() {
        // Arrange
        testInquiry.setName("A".repeat(150)); // Max length
        testInquiry.setEmail("a".repeat(190) + "@email.com"); // ~200 chars
        testInquiry.setPhone("1".repeat(30)); // Max length

        // Act
        Inquiry saved = inquiryRepository.save(testInquiry);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(150, saved.getName().length());
        assertEquals(30, saved.getPhone().length());
    }
}
