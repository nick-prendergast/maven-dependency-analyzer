package io.mend.maven.analyzer.service.detection;

import io.mend.maven.analyzer.config.MavenConstants;
import io.mend.maven.analyzer.exception.MavenProjectException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PomParser {
    private static final Pattern PROPERTY_PLACEHOLDER_PATTERN = Pattern.compile(MavenConstants.PROPERTY_PLACEHOLDER_REGEX);
    
    public Model parsePomXml(@NonNull File pomFile) throws MavenProjectException {
        try {
            MavenXpp3Reader xmlReader = new MavenXpp3Reader();
            Model pomModel;
            
            try (FileInputStream fileInputStream = new FileInputStream(pomFile)) {
                pomModel = xmlReader.read(fileInputStream);
            }
            
            replacePropertyPlaceholders(pomModel);
            validateRequiredFields(pomModel);
            
            log.debug("Successfully parsed POM. Dependencies: {}", 
                pomModel.getDependencies() != null ? pomModel.getDependencies().size() : 0);
            
            return pomModel;
            
        } catch (IOException e) {
            throw new MavenProjectException("Failed to read pom.xml file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new MavenProjectException("Failed to parse pom.xml: " + e.getMessage(), e);
        }
    }
    
    private void replacePropertyPlaceholders(Model pomModel) {
        if (pomModel.getProperties() == null || pomModel.getDependencies() == null) {
            return;
        }
        
        Properties availableProperties = new Properties();
        availableProperties.putAll(pomModel.getProperties());
        
        if (pomModel.getDependencies() != null) {
            pomModel.getDependencies().forEach(dependency -> 
                replaceDependencyProperties(dependency, availableProperties));
        }
        
        if (hasDependencyManagement(pomModel)) {
            pomModel.getDependencyManagement().getDependencies().forEach(dependency -> 
                replaceDependencyProperties(dependency, availableProperties));
        }
    }
    
    private boolean hasDependencyManagement(Model model) {
        return model.getDependencyManagement() != null && 
               model.getDependencyManagement().getDependencies() != null;
    }
    
    private String replacePlaceholders(String textWithPlaceholders, Properties properties) {
        if (textWithPlaceholders == null || !textWithPlaceholders.contains(MavenConstants.PROPERTY_PLACEHOLDER_PREFIX)) {
            return textWithPlaceholders;
        }
        
        Matcher placeholderMatcher = PROPERTY_PLACEHOLDER_PATTERN.matcher(textWithPlaceholders);
        StringBuilder replacedText = new StringBuilder();
        
        while (placeholderMatcher.find()) {
            String propertyKey = placeholderMatcher.group(1);
            String propertyValue = properties.getProperty(propertyKey);
            
            if (propertyValue != null) {
                placeholderMatcher.appendReplacement(replacedText, Matcher.quoteReplacement(propertyValue));
                log.debug("Replaced ${{}}: {}", propertyKey, propertyValue);
            } else {
                log.warn("Property not found: ${{}}", propertyKey);
                placeholderMatcher.appendReplacement(replacedText, Matcher.quoteReplacement(placeholderMatcher.group(0)));
            }
        }
        
        placeholderMatcher.appendTail(replacedText);
        return replacedText.toString();
    }
    
    private void validateRequiredFields(Model pomModel) throws MavenProjectException {
        if (hasNoGroupId(pomModel)) {
            throw new MavenProjectException("No groupId specified in pom.xml");
        }
        
        if (hasNoArtifactId(pomModel)) {
            throw new MavenProjectException("No artifactId specified in pom.xml");
        }
    }
    
    private boolean hasNoGroupId(Model model) {
        return model.getGroupId() == null && 
               (model.getParent() == null || model.getParent().getGroupId() == null);
    }
    
    private boolean hasNoArtifactId(Model model) {
        return model.getArtifactId() == null || model.getArtifactId().trim().isEmpty();
    }
    
    private void replaceDependencyProperties(@NonNull org.apache.maven.model.Dependency dependency, @NonNull Properties properties) {
        if (dependency.getVersion() != null) {
            dependency.setVersion(replacePlaceholders(dependency.getVersion(), properties));
        }
        if (dependency.getGroupId() != null) {
            dependency.setGroupId(replacePlaceholders(dependency.getGroupId(), properties));
        }
        if (dependency.getArtifactId() != null) {
            dependency.setArtifactId(replacePlaceholders(dependency.getArtifactId(), properties));
        }
    }
}