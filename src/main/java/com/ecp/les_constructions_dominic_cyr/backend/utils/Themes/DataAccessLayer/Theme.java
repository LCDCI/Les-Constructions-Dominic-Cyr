package com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.DataAccessLayer;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_theme")
@Data
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "theme_name", unique = true, nullable = false)
    private String themeName;

    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String cardBackground;
    private String backgroundColor;
    private String textPrimary;
    private String white;

    private String borderRadius;
    private String boxShadow;
    private String transition;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}