package com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.PresentationLayer.ThemeResponseDTO;


public interface ThemeService {

    ThemeResponseDTO getTheme(String themeName);
}
