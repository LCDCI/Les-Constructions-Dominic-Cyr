package com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Optional<Theme> findByThemeName(String themeName);
}