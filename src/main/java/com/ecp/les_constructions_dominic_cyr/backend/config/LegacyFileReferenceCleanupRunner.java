package com.ecp.les_constructions_dominic_cyr.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LegacyFileReferenceCleanupRunner implements ApplicationRunner {

    private static final List<String> REMOVED_FILE_IDS = List.of(
            "dcada4a5-aa19-4346-934e-1e57bc0f9e1f",
            "4215eb96-af4b-492b-870a-6925e78b7fcc",
            "ee576ed6-5d56-4d54-ba25-7157f7b75d0d",
            "ae470899-8adf-49f9-b170-5c1ea611493a"
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        for (String fileId : REMOVED_FILE_IDS) {
            clearProjectReferences(fileId);
            clearImageReferences("houses", "image_identifier", fileId);
            clearImageReferences("realizations", "image_identifier", fileId);
            clearImageReferences("site_content", "image_identifier", fileId);
            deleteGalleryReferences(fileId);
        }
    }

    private void clearProjectReferences(String fileId) {
        jdbcTemplate.update(
                "UPDATE projects SET image_identifier = NULL WHERE image_identifier = ?",
                fileId
        );
        jdbcTemplate.update(
                "UPDATE projects SET banner_image_identifier = NULL WHERE banner_image_identifier = ?",
                fileId
        );
    }

    private void clearImageReferences(String table, String column, String fileId) {
        jdbcTemplate.update(
                "UPDATE " + table + " SET " + column + " = NULL WHERE " + column + " = ?",
                fileId
        );
    }

    private void deleteGalleryReferences(String fileId) {
        jdbcTemplate.update(
                "DELETE FROM project_gallery_images WHERE image_identifier = ?",
                fileId
        );
    }
}
