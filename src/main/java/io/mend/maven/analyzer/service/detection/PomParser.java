package io.mend.maven.analyzer.service.detection;

import io.mend.maven.analyzer.exception.MavenProjectException;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.*;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Parses Maven POM files and creates Model objects with proper property interpolation.
 * 
 * This service handles the parsing of pom.xml files using Maven's ModelBuilder
 * which properly resolves properties, handles parent POM inheritance, and 
 * interpolates property placeholders in dependency versions.
 */
public class PomParser {
    
    private static final Logger logger = LoggerFactory.getLogger(PomParser.class);
    
    /**
     * Parses a POM file and returns the Maven Model with properties interpolated.
     */
    public Model parsePomXml(File pomFile) throws MavenProjectException {
        try {
            // First try with ModelBuilder for full property resolution
            ModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();
            
            ModelBuildingRequest request = new DefaultModelBuildingRequest();
            request.setPomFile(pomFile);
            request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            request.setProcessPlugins(false);
            request.setTwoPhaseBuilding(false);
            request.setSystemProperties(System.getProperties());
            request.setModelResolver(new NoOpModelResolver());
            
            try {
                ModelBuildingResult result = modelBuilder.build(request);
                Model effectiveModel = result.getEffectiveModel();
                
                validateModel(effectiveModel);
                
                logger.debug("Successfully parsed POM with ModelBuilder. Dependencies: {}", 
                    effectiveModel.getDependencies() != null ? effectiveModel.getDependencies().size() : 0);
                
                return effectiveModel;
                
            } catch (ModelBuildingException e) {
                logger.warn("ModelBuilder failed (likely parent POM issue), falling back to manual interpolation: {}", 
                    e.getMessage());
                
                // Fallback to manual property interpolation
                return parseWithManualInterpolation(pomFile);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error parsing POM {}: {}", pomFile.getAbsolutePath(), e.getMessage());
            throw new MavenProjectException("Failed to parse pom.xml: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fallback method that manually interpolates properties when ModelBuilder fails.
     */
    private Model parseWithManualInterpolation(File pomFile) throws MavenProjectException {
        try {
            // Use the basic XML reader and then manually interpolate properties
            org.apache.maven.model.io.xpp3.MavenXpp3Reader reader = new org.apache.maven.model.io.xpp3.MavenXpp3Reader();
            Model model;
            
            try (java.io.FileInputStream fileInputStream = new java.io.FileInputStream(pomFile)) {
                model = reader.read(fileInputStream);
            }
            
            // Manually interpolate properties in the model
            interpolateProperties(model);
            
            validateModel(model);
            
            logger.debug("Successfully parsed POM with manual interpolation. Dependencies: {}", 
                model.getDependencies() != null ? model.getDependencies().size() : 0);
            
            return model;
            
        } catch (IOException e) {
            throw new MavenProjectException("Failed to read pom.xml file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new MavenProjectException("Failed to parse pom.xml with manual interpolation: " + e.getMessage(), e);
        }
    }
    
    /**
     * Manually interpolates properties in the Maven model.
     */
    private void interpolateProperties(Model model) {
        if (model.getProperties() == null || model.getDependencies() == null) {
            return;
        }
        
        // Build a map of all available properties
        java.util.Properties allProperties = new java.util.Properties();
        
        // Add model properties
        allProperties.putAll(model.getProperties());
        
        // Add system properties (lower priority)
        System.getProperties().forEach((key, value) -> {
            if (!allProperties.containsKey(key)) {
                allProperties.put(key, value);
            }
        });
        
        // Interpolate dependencies
        for (org.apache.maven.model.Dependency dependency : model.getDependencies()) {
            if (dependency.getVersion() != null) {
                dependency.setVersion(interpolateString(dependency.getVersion(), allProperties));
            }
            if (dependency.getGroupId() != null) {
                dependency.setGroupId(interpolateString(dependency.getGroupId(), allProperties));
            }
            if (dependency.getArtifactId() != null) {
                dependency.setArtifactId(interpolateString(dependency.getArtifactId(), allProperties));
            }
        }
        
        // Interpolate dependency management
        if (model.getDependencyManagement() != null && model.getDependencyManagement().getDependencies() != null) {
            for (org.apache.maven.model.Dependency dependency : model.getDependencyManagement().getDependencies()) {
                if (dependency.getVersion() != null) {
                    dependency.setVersion(interpolateString(dependency.getVersion(), allProperties));
                }
                if (dependency.getGroupId() != null) {
                    dependency.setGroupId(interpolateString(dependency.getGroupId(), allProperties));
                }
                if (dependency.getArtifactId() != null) {
                    dependency.setArtifactId(interpolateString(dependency.getArtifactId(), allProperties));
                }
            }
        }
    }
    
    /**
     * Interpolates property placeholders in a string.
     */
    private String interpolateString(String str, java.util.Properties properties) {
        if (str == null || !str.contains("${")) {
            return str;
        }
        
        String result = str;
        // Simple property interpolation - handles ${property.name} patterns
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(str);
        
        while (matcher.find()) {
            String propertyName = matcher.group(1);
            String propertyValue = properties.getProperty(propertyName);
            
            if (propertyValue != null) {
                result = result.replace("${" + propertyName + "}", propertyValue);
                logger.debug("Interpolated property ${{{}}}: {} -> {}", propertyName, "${" + propertyName + "}", propertyValue);
            } else {
                logger.warn("Could not resolve property: ${{{}}}", propertyName);
            }
        }
        
        return result;
    }
    
    /**
     * Validates that the parsed model has required fields.
     */
    private void validateModel(Model model) throws MavenProjectException {
        if (model.getGroupId() == null && (model.getParent() == null || model.getParent().getGroupId() == null)) {
            throw new MavenProjectException("No groupId specified in pom.xml");
        }
        
        if (model.getArtifactId() == null || model.getArtifactId().trim().isEmpty()) {
            throw new MavenProjectException("No artifactId specified in pom.xml");
        }
    }
    
    /**
     * No-op model resolver that doesn't resolve parent POMs.
     * This allows the ModelBuilder to work without requiring remote parent POM access.
     */
    private static class NoOpModelResolver implements ModelResolver {
        
        @Override
        public ModelSource resolveModel(String groupId, String artifactId, String version) 
                throws UnresolvableModelException {
            // For basic functionality, we'll handle commonly available parent POMs
            // For advanced use cases, this could be enhanced to use Maven repositories
            throw new UnresolvableModelException("Model resolution not implemented for remote POMs", 
                groupId, artifactId, version);
        }
        
        @Override
        public ModelSource resolveModel(org.apache.maven.model.Parent parent) 
                throws UnresolvableModelException {
            return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
        }
        
        @Override
        public ModelSource resolveModel(org.apache.maven.model.Dependency dependency)
                throws UnresolvableModelException {
            return resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
        }
        
        @Override
        public void addRepository(org.apache.maven.model.Repository repository) 
                throws org.apache.maven.model.resolution.InvalidRepositoryException {
            // Not implemented for basic functionality
        }
        
        @Override
        public void addRepository(org.apache.maven.model.Repository repository, boolean replace) 
                throws org.apache.maven.model.resolution.InvalidRepositoryException {
            // Not implemented for basic functionality
        }
        
        @Override
        public ModelResolver newCopy() {
            return new NoOpModelResolver();
        }
    }
}