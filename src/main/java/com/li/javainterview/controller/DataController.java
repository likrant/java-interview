package com.li.javainterview.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@RestController
public class DataController {
    private final ResourceLoader resourceLoader;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public DataController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/data/**")
    public ResponseEntity<Resource> getData(HttpServletRequest request) throws IOException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String relativePath = matcher.extractPathWithinPattern(bestMatchPattern, path);
        Resource resource = resourceLoader.getResource("classpath:data/" + relativePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        MediaType type = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(type).body(resource);
    }
}
