package com.nexblog.backend.controller;

import com.nexblog.backend.service.publish.PublishService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
public class PublishPreviewController {

    private final PublishService publishService;

    public PublishPreviewController(PublishService publishService) {
        this.publishService = publishService;
    }

    @GetMapping({"/preview/{jobId}", "/preview/{jobId}/", "/preview/{jobId}/{*relativePath}"})
    public ResponseEntity<Resource> preview(
        @PathVariable String jobId,
        @PathVariable(required = false) String relativePath
    ) {
        Path previewFile = publishService.resolvePreviewFile(jobId, relativePath);
        Resource resource = new FileSystemResource(previewFile);
        MediaType mediaType = MediaTypeFactory.getMediaType(previewFile.getFileName().toString())
            .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(resource);
    }
}
