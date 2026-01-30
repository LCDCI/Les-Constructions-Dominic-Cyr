package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.LivingEnvironment;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.LivingEnvironment.LivingEnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectIdentifier}/living-environment")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LivingEnvironmentController {

    private final LivingEnvironmentService livingEnvironmentService;

    @GetMapping
    public ResponseEntity<LivingEnvironmentResponseModel> getLivingEnvironment(
            @PathVariable String projectIdentifier,
            @RequestParam(required = false, defaultValue = "en") String lang) {
        
        log.info("GET /api/v1/projects/{}/living-environment?lang={}", projectIdentifier, lang);
        
        LivingEnvironmentResponseModel response = livingEnvironmentService.getLivingEnvironment(projectIdentifier, lang);
        
        return ResponseEntity.ok(response);
    }
}
