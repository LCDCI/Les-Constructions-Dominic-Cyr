package com.ecp.les_constructions_dominic_cyr.backend.utils.translation.mapperlayer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationKeyResponse {
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("value")
    private String value;
    
    @JsonProperty("language")
    private String language;
}

