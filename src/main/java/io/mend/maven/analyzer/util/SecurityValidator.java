package io.mend.maven.analyzer.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple utility for basic input validation.
 */
public final class SecurityValidator {
    
    private SecurityValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Basic path validation and normalization.
     */
    public static Path validateAndNormalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        
        Path normalized = Paths.get(path.trim()).normalize();
        
        // Basic check for obvious path traversal attempts
        if (normalized.toString().contains("..")) {
            throw new SecurityException("Path traversal attempt detected: " + path);
        }
        
        return normalized;
    }
}