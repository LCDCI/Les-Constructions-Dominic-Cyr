package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.LotDocument;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LotDocumentRepository extends JpaRepository<LotDocument, UUID> {

    /**
     * Find all documents for a specific lot.
     */
    @Query("SELECT ld FROM LotDocument ld WHERE ld.lot.lotIdentifier.lotId = :lotId ORDER BY ld.uploadedAt DESC")
    List<LotDocument> findByLotId(@Param("lotId") UUID lotId);

    /**
     * Find documents by lot and uploader.
     */
    @Query("SELECT ld FROM LotDocument ld WHERE ld.lot.lotIdentifier.lotId = :lotId AND ld.uploader.userIdentifier.userId = :uploaderId ORDER BY ld.uploadedAt DESC")
    List<LotDocument> findByLotIdAndUploaderId(@Param("lotId") UUID lotId, @Param("uploaderId") UUID uploaderId);

    /**
     * Find documents by lot and type (image or not).
     */
    @Query("SELECT ld FROM LotDocument ld WHERE ld.lot.lotIdentifier.lotId = :lotId AND ld.isImage = :isImage ORDER BY ld.uploadedAt DESC")
    List<LotDocument> findByLotIdAndType(@Param("lotId") UUID lotId, @Param("isImage") Boolean isImage);

    /**
     * Search documents by lot and filename substring (case-insensitive).
     */
    @Query("SELECT ld FROM LotDocument ld WHERE ld.lot.lotIdentifier.lotId = :lotId AND LOWER(ld.originalFileName) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY ld.uploadedAt DESC")
    List<LotDocument> searchByLotIdAndFileName(@Param("lotId") UUID lotId, @Param("search") String search);

    /**
     * Check if a document belongs to a specific lot (for security validation).
     */
    @Query("SELECT CASE WHEN COUNT(ld) > 0 THEN true ELSE false END FROM LotDocument ld WHERE ld.id = :documentId AND ld.lot.lotIdentifier.lotId = :lotId")
    boolean existsByIdAndLotId(@Param("documentId") UUID documentId, @Param("lotId") UUID lotId);
}
