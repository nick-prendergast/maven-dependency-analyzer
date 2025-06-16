package io.mend.maven.analyzer.service.detection;

import io.mend.maven.analyzer.exception.MavenProjectException;
import lombok.NonNull;
import org.apache.maven.model.Model;

import java.nio.file.Path;

/**
 * Service that coordinates Maven project detection and POM processing.
 * 
 * This service acts as a facade for the Maven project detection subsystem,
 * orchestrating the validation, parsing, and property extraction of Maven
 * projects. It delegates specific responsibilities to specialized services
 * following the single responsibility principle.
 */
public class MavenProjectDetectionService {
    
    private final PomValidator validator;
    private final PomParser parser;
    private final ModelPropertyExtractor extractor;
    
    public MavenProjectDetectionService() {
        this.validator = new PomValidator();
        this.parser = new PomParser();
        this.extractor = new ModelPropertyExtractor();
    }
    
    /**
     * Reads and parses the POM file from the given project path.
     */
    public Model readPomModel(@NonNull String projectPath) throws MavenProjectException {
        Path pomPath = validator.validatePomPath(projectPath);
        return parser.parsePomXml(pomPath.toFile());
    }
    
    
    /**
     * Gets the effective groupId from the model.
     */
    public String getEffectiveGroupId(@NonNull Model model) {
        return extractor.getEffectiveGroupId(model);
    }
    
    /**
     * Gets the effective version from the model.
     */
    public String getEffectiveVersion(@NonNull Model model) {
        return extractor.getEffectiveVersion(model);
    }
}