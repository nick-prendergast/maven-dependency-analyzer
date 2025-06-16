package io.mend.maven.analyzer.service;

import io.mend.maven.analyzer.config.MavenResolverConfig;
import io.mend.maven.analyzer.exception.DependencyAnalysisException;
import io.mend.maven.analyzer.exception.MavenProjectException;
import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import io.mend.maven.analyzer.model.response.AnalysisResult;
import io.mend.maven.analyzer.service.analysis.DependencyResolverService;
import io.mend.maven.analyzer.service.analysis.DependencyTreeBuilderService;
import io.mend.maven.analyzer.service.detection.MavenProjectDetectionService;
import io.mend.maven.analyzer.service.hash.Sha1HashService;
import io.mend.maven.analyzer.util.SecurityValidator;
import org.apache.maven.model.Model;
import org.eclipse.aether.graph.DependencyNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

/**
 * Service that orchestrates the complete Maven dependency analysis process.
 */
@Slf4j
public class DependencyAnalysisService {
    
    private final MavenProjectDetectionService detectionService;
    private final DependencyResolverService resolverService;
    private final DependencyTreeBuilderService treeBuilderService;
    
    public DependencyAnalysisService(@NonNull MavenResolverConfig config) {
        this.detectionService = new MavenProjectDetectionService();
        this.resolverService = new DependencyResolverService(config);
        this.treeBuilderService = new DependencyTreeBuilderService(new Sha1HashService(config));
    }
    
    /**
     * Analyzes a Maven project and returns the dependency tree with SHA1 hashes.
     */
    public AnalysisResult analyze(@NonNull String projectPath) throws DependencyAnalysisException {
        try {
            // Basic path validation
            Path normalizedPath = SecurityValidator.validateAndNormalizePath(projectPath);
            String safePath = normalizedPath.toString();
            
            // Parse POM
            Model projectModel = detectionService.readPomModel(safePath);
            
            // Resolve dependencies
            DependencyNode rootNode = resolverService.resolveDependencies(projectModel);
            
            // Build dependency tree with SHA1 hashes  
            List<AnalyzedDependency> dependencies =
                treeBuilderService.buildDependencyTree(rootNode);
            
            // Create result
            String projectGroupId = detectionService.getEffectiveGroupId(projectModel);
            String projectArtifactId = projectModel.getArtifactId();
            String projectVersion = detectionService.getEffectiveVersion(projectModel);
            
            AnalysisResult result = new AnalysisResult(safePath, projectGroupId, projectArtifactId, projectVersion);
            result.setDependencies(dependencies);
            
            log.debug("Analysis completed. Found {} dependencies", result.getTotalDependencies());
            return result;
            
        } catch (DependencyAnalysisException e) {
            throw e;
        } catch (MavenProjectException e) {
            throw new DependencyAnalysisException("Maven project validation failed: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected runtime error during analysis", e);
            throw new DependencyAnalysisException("Analysis failed due to unexpected error: " + e.getMessage(), e);
        }
    }
    
}