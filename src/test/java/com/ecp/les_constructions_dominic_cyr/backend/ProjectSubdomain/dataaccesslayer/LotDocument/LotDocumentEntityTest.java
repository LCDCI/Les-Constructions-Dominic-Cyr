package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.dataaccesslayer.LotDocument;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.Lot;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument.LotDocument;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LotDocumentEntityTest {

    @Test
    void constructor_SetsAllFields() {
        Lot lot = new Lot();
        lot.setLotIdentifier(new LotIdentifier(UUID.randomUUID().toString()));
        Users uploader = new Users();
        uploader.setUserIdentifier(UserIdentifier.newId());

        LotDocument doc = new LotDocument(
            lot, uploader, "John Doe", "plan.pdf", "storage-key-1",
            "application/pdf", 1024L, false
        );

        assertSame(lot, doc.getLot());
        assertSame(uploader, doc.getUploader());
        assertEquals("John Doe", doc.getUploaderName());
        assertEquals("plan.pdf", doc.getOriginalFileName());
        assertEquals("storage-key-1", doc.getStorageKey());
        assertEquals("application/pdf", doc.getMimeType());
        assertEquals(1024L, doc.getSizeBytes());
        assertFalse(doc.getIsImage());
    }

    @Test
    void settersAndGetters_Work() {
        LotDocument doc = new LotDocument();
        Lot lot = new Lot();
        Users uploader = new Users();
        doc.setLot(lot);
        doc.setUploader(uploader);
        doc.setUploaderName("Jane");
        doc.setOriginalFileName("doc.pdf");
        doc.setStorageKey("key");
        doc.setMimeType("application/pdf");
        doc.setSizeBytes(2048L);
        doc.setIsImage(true);

        assertSame(lot, doc.getLot());
        assertSame(uploader, doc.getUploader());
        assertEquals("Jane", doc.getUploaderName());
        assertEquals("doc.pdf", doc.getOriginalFileName());
        assertEquals("key", doc.getStorageKey());
        assertEquals("application/pdf", doc.getMimeType());
        assertEquals(2048L, doc.getSizeBytes());
        assertTrue(doc.getIsImage());
    }
}
