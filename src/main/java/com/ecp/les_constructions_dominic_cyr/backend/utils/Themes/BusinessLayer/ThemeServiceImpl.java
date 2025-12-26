package com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.DataAccessLayer.Theme;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.DataAccessLayer.ThemeRepository;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.PresentationLayer.ThemeResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class ThemeServiceImpl implements ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeServiceImpl(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Override
    public ThemeResponseDTO getTheme(String themeName) {

        Theme theme = themeRepository.findByThemeName(themeName)
                .orElseThrow(() -> new RuntimeException("Theme not found"));

        ThemeResponseDTO dto = new ThemeResponseDTO();
        dto.primaryColor = theme.getPrimaryColor();
        dto.secondaryColor = theme.getSecondaryColor();
        dto.accentColor = theme.getAccentColor();
        dto.cardBackground = theme.getCardBackground();
        dto.backgroundColor = theme.getBackgroundColor();
        dto.textPrimary = theme.getTextPrimary();
        dto.white = theme.getWhite();

        dto.borderRadius = theme.getBorderRadius();
        dto.boxShadow = theme.getBoxShadow();
        dto.transition = theme.getTransition();

        return dto;
    }
}
