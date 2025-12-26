package com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.utils.Themes.BusinessLayer.ThemeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/theme")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ThemeResponseDTO getTheme(
            @RequestParam(defaultValue = "default") String name
    ) {
        return themeService.getTheme(name);
    }
}

