package io.mend.maven.analyzer.service.detection;

import io.mend.maven.analyzer.config.MavenConstants;
import io.mend.maven.analyzer.exception.MavenProjectException;
import lombok.NonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates Maven project structure and POM file existence.
 * 
 * This service is responsible for validating that a given project path
 * contains a valid Maven project structure with a readable pom.xml file.
 * It performs path validation, existence checks, and file type verification.
 */
public class PomValidator {
    
    
    /**
     * Validates the project path and returns the path to the pom.xml file.
     */
    public Path validatePomPath(@NonNull String projectPath) throws MavenProjectException {
        Path projectDir = validateAndGetProjectPath(projectPath);
        Path pomPath = projectDir.resolve(MavenConstants.POM_XML_FILENAME);
        
        if (!Files.exists(pomPath)) {
            throw new MavenProjectException("No pom.xml found in project directory: " + projectPath);
        }
        
        if (!Files.isRegularFile(pomPath)) {
            throw new MavenProjectException("pom.xml is not a regular file: " + pomPath);
        }
        
        return pomPath;
    }
    
    /**
     * Validates that the project path exists and is a directory.
     */
    private Path validateAndGetProjectPath(@NonNull String projectPath) throws MavenProjectException {
        if (projectPath.trim().isEmpty()) {
            throw new MavenProjectException("Project path cannot be empty");
        }
        
        Path path = Paths.get(projectPath);
        if (!Files.exists(path)) {
            throw new MavenProjectException("Project path does not exist: " + projectPath);
        }
        
        if (!Files.isDirectory(path)) {
            throw new MavenProjectException("Project path is not a directory: " + projectPath);
        }
        
        return path;
    }
}