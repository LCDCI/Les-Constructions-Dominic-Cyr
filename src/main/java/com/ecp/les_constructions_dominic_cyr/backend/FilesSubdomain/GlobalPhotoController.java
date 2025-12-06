package com.ecp.les_constructions_dominic_cyr.backend.FilesSubdomain;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/photos")
@CrossOrigin(origins = "http://localhost:3000")
public class GlobalPhotoController {

    private final GlobalPhotoService service;

    public GlobalPhotoController(GlobalPhotoService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> uploadGlobal(
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "X-User", defaultValue = "demo-user") String userId
    ) {
        return service.uploadGlobalPhoto(file, userId);
    }
}