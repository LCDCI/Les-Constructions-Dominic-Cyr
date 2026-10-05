package com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SiteContentRepository extends JpaRepository<SiteContent, Long> {
    List<SiteContent> findAllByLanguageOrderByPageGroupAscSortOrderAscPageKeyAsc(String language);

    List<SiteContent> findAllByLanguageAndPageGroupOrderBySortOrderAscPageKeyAsc(
            String language,
            String pageGroup
    );

        @Query("select distinct s.pageGroup from SiteContent s where s.language = :language order by s.pageGroup asc")
        List<String> findPageGroupsByLanguage(@Param("language") String language);

    Optional<SiteContent> findByPageGroupAndPageKeyAndLanguage(
            String pageGroup,
            String pageKey,
            String language
    );
}
