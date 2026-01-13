package com.ecp.les_constructions_dominic_cyr.backend.enums;

import com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer.NotificationType;
import com.ecp. les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.DocumentType;
import com.ecp.les_constructions_dominic_cyr. backend.ProjectSubdomain.DataAccessLayer.ImageType;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Project. ProjectStatus;
import org.junit.jupiter. api.Test;

import static org.junit.jupiter.api. Assertions.*;

public class EnumTest {

    @Test
    void documentType_HasAllExpectedValues() {
        DocumentType[] values = DocumentType.values();

        assertEquals(4, values.length);
        assertNotNull(DocumentType.PDF);
        assertNotNull(DocumentType. DOCX);
        assertNotNull(DocumentType.TXT);
        assertNotNull(DocumentType. XLSX);
    }

    @Test
    void documentType_ValueOf_ReturnsCorrectEnum() {
        assertEquals(DocumentType.PDF, DocumentType.valueOf("PDF"));
        assertEquals(DocumentType. DOCX, DocumentType.valueOf("DOCX"));
        assertEquals(DocumentType. TXT, DocumentType.valueOf("TXT"));
        assertEquals(DocumentType.XLSX, DocumentType.valueOf("XLSX"));
    }

    @Test
    void documentType_Name_ReturnsCorrectString() {
        assertEquals("PDF", DocumentType.PDF.name());
        assertEquals("DOCX", DocumentType.DOCX.name());
        assertEquals("TXT", DocumentType.TXT.name());
        assertEquals("XLSX", DocumentType.XLSX.name());
    }

    @Test
    void documentType_Ordinal_ReturnsCorrectIndex() {
        assertEquals(0, DocumentType.PDF.ordinal());
        assertEquals(1, DocumentType. DOCX.ordinal());
        assertEquals(2, DocumentType.TXT.ordinal());
        assertEquals(3, DocumentType.XLSX.ordinal());
    }

    @Test
    void imageType_HasAllExpectedValues() {
        ImageType[] values = ImageType.values();

        assertEquals(5, values.length);
        assertNotNull(ImageType.PNG);
        assertNotNull(ImageType. JPEG);
        assertNotNull(ImageType. WEBP);
        assertNotNull(ImageType.HEIC);
        assertNotNull(ImageType. BLOB);
    }

    @Test
    void imageType_ValueOf_ReturnsCorrectEnum() {
        assertEquals(ImageType.PNG, ImageType.valueOf("PNG"));
        assertEquals(ImageType. JPEG, ImageType. valueOf("JPEG"));
        assertEquals(ImageType.WEBP, ImageType.valueOf("WEBP"));
        assertEquals(ImageType.HEIC, ImageType.valueOf("HEIC"));
        assertEquals(ImageType.BLOB, ImageType.valueOf("BLOB"));
    }

    @Test
    void imageType_Name_ReturnsCorrectString() {
        assertEquals("PNG", ImageType. PNG.name());
        assertEquals("JPEG", ImageType. JPEG.name());
        assertEquals("WEBP", ImageType. WEBP.name());
        assertEquals("HEIC", ImageType.HEIC.name());
        assertEquals("BLOB", ImageType.BLOB.name());
    }

    @Test
    void imageType_Ordinal_ReturnsCorrectIndex() {
        assertEquals(0, ImageType.PNG.ordinal());
        assertEquals(1, ImageType.JPEG.ordinal());
        assertEquals(2, ImageType.WEBP. ordinal());
        assertEquals(3, ImageType.HEIC.ordinal());
        assertEquals(4, ImageType.BLOB.ordinal());
    }



    @Test
    void notificationType_HasAllExpectedValues() {
        NotificationType[] values = NotificationType.values();

        assertEquals(2, values.length);
        assertNotNull(NotificationType. EMAIL);
        assertNotNull(NotificationType.SYSTEM_ALERT);
    }

    @Test
    void notificationType_ValueOf_ReturnsCorrectEnum() {
        assertEquals(NotificationType.EMAIL, NotificationType.valueOf("EMAIL"));
        assertEquals(NotificationType. SYSTEM_ALERT, NotificationType.valueOf("SYSTEM_ALERT"));
    }

    @Test
    void notificationType_Name_ReturnsCorrectString() {
        assertEquals("EMAIL", NotificationType.EMAIL.name());
        assertEquals("SYSTEM_ALERT", NotificationType. SYSTEM_ALERT. name());
    }

    @Test
    void notificationType_Ordinal_ReturnsCorrectIndex() {
        assertEquals(0, NotificationType.EMAIL. ordinal());
        assertEquals(1, NotificationType.SYSTEM_ALERT.ordinal());
    }

    @Test
    void projectStatus_HasAllExpectedValues() {
        ProjectStatus[] values = ProjectStatus.values();

        assertEquals(5, values.length);
        assertNotNull(ProjectStatus. PLANNED);
        assertNotNull(ProjectStatus. IN_PROGRESS);
        assertNotNull(ProjectStatus. COMPLETED);
        assertNotNull(ProjectStatus. DELAYED);
        assertNotNull(ProjectStatus. CANCELLED);
    }

    @Test
    void projectStatus_ValueOf_ReturnsCorrectEnum() {
        assertEquals(ProjectStatus. PLANNED, ProjectStatus. valueOf("PLANNED"));
        assertEquals(ProjectStatus.IN_PROGRESS, ProjectStatus. valueOf("IN_PROGRESS"));
        assertEquals(ProjectStatus. COMPLETED, ProjectStatus. valueOf("COMPLETED"));
        assertEquals(ProjectStatus.DELAYED, ProjectStatus.valueOf("DELAYED"));
        assertEquals(ProjectStatus. CANCELLED, ProjectStatus.valueOf("CANCELLED"));
    }

    @Test
    void projectStatus_Name_ReturnsCorrectString() {
        assertEquals("PLANNED", ProjectStatus. PLANNED.name());
        assertEquals("IN_PROGRESS", ProjectStatus.IN_PROGRESS.name());
        assertEquals("COMPLETED", ProjectStatus.COMPLETED.name());
        assertEquals("DELAYED", ProjectStatus.DELAYED.name());
        assertEquals("CANCELLED", ProjectStatus.CANCELLED.name());
    }

    @Test
    void projectStatus_Ordinal_ReturnsCorrectIndex() {
        assertEquals(0, ProjectStatus.PLANNED. ordinal());
        assertEquals(1, ProjectStatus.IN_PROGRESS.ordinal());
        assertEquals(2, ProjectStatus. COMPLETED.ordinal());
        assertEquals(3, ProjectStatus. DELAYED.ordinal());
        assertEquals(4, ProjectStatus. CANCELLED.ordinal());
    }

    @Test
    void documentType_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DocumentType.valueOf("INVALID");
        });
    }

    @Test
    void imageType_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ImageType.valueOf("INVALID");
        });
    }

    @Test
    void notificationType_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationType.valueOf("INVALID");
        });
    }

    @Test
    void projectStatus_InvalidValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ProjectStatus.valueOf("INVALID");
        });
    }

    @Test
    void allEnums_CanBeIteratedOver() {
        for (DocumentType type : DocumentType.values()) {
            assertNotNull(type. name());
        }
        for (ImageType type : ImageType.values()) {
            assertNotNull(type. name());
        }
        for (NotificationType type : NotificationType.values()) {
            assertNotNull(type.name());
        }
        for (ProjectStatus status : ProjectStatus.values()) {
            assertNotNull(status.name());
        }
    }

    @Test
    void allEnums_ToStringReturnsName() {
        assertEquals("PDF", DocumentType.PDF.toString());
        assertEquals("PNG", ImageType.PNG.toString());
        assertEquals("EMAIL", NotificationType. EMAIL.toString());
        assertEquals("PLANNED", ProjectStatus. PLANNED.toString());
    }

    @Test
    void enums_CompareByOrdinal() {
        assertTrue(DocumentType.PDF.compareTo(DocumentType.DOCX) < 0);
        assertTrue(ImageType. JPEG.compareTo(ImageType.PNG) > 0);
    }

    @Test
    void enums_Equality() {
        assertEquals(DocumentType.PDF, DocumentType.PDF);
        assertEquals(ImageType.PNG, ImageType.PNG);
        assertEquals(NotificationType.EMAIL, NotificationType.EMAIL);
        assertEquals(ProjectStatus.COMPLETED, ProjectStatus. COMPLETED);
    }

    @Test
    void enums_NotEqual() {
        assertNotEquals(DocumentType.PDF, DocumentType.DOCX);
        assertNotEquals(ImageType. PNG, ImageType. JPEG);
        assertNotEquals(NotificationType. EMAIL, NotificationType.SYSTEM_ALERT);
        assertNotEquals(ProjectStatus.PLANNED, ProjectStatus. COMPLETED);
    }

    @Test
    void documentType_HashCode_IsConsistent() {
        int hash1 = DocumentType. PDF.hashCode();
        int hash2 = DocumentType. PDF.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    void imageType_HashCode_IsConsistent() {
        int hash1 = ImageType.PNG.hashCode();
        int hash2 = ImageType.PNG.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    void enums_GetDeclaringClass() {
        assertEquals(DocumentType.class, DocumentType.PDF.getDeclaringClass());
        assertEquals(ImageType.class, ImageType.PNG.getDeclaringClass());
        assertEquals(NotificationType. class, NotificationType.EMAIL.getDeclaringClass());
        assertEquals(ProjectStatus.class, ProjectStatus.PLANNED.getDeclaringClass());
    }
}