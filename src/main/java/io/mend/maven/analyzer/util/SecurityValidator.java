package io.mend.maven.analyzer.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class SecurityValidator {
    
    public static Path validateAndNormalizePath(@NonNull String inputPath) {
        if (inputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        
        String cleanPath = inputPath.trim();
        if (containsPathTraversalAttempt(cleanPath)) {
            throw new SecurityException("Path traversal attempt detected: " + inputPath);
        }
        
        Path normalizedPath = Paths.get(cleanPath).normalize();
        Path absolutePath = normalizedPath.toAbsolutePath();
        
        ensurePathIsValid(absolutePath, inputPath);
        
        return absolutePath;
    }
    
    private static boolean containsPathTraversalAttempt(String path) {
        return path.contains("..") || path.contains("~");
    }
    
    private static void ensurePathIsValid(Path absolutePath, String originalPath) {
        try {
            File file = absolutePath.toFile();
            String canonicalPath = file.getCanonicalPath();
            String absolutePathStr = absolutePath.toString();
            
            if (!canonicalPath.equals(absolutePathStr)) {
                throw new SecurityException("Path contains symbolic links or other indirection: " + originalPath);
            }
        } catch (IOException e) {
            throw new SecurityException("Invalid path: " + originalPath);
        }
    }
}