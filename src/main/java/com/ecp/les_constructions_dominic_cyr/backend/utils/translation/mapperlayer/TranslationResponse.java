package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for translation response.
 * Contains all translations for a specific language.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {
    
    @JsonProperty("language")
    private String language;
    
    @JsonProperty("translations")
    private Map<String, Object> translations;
}

