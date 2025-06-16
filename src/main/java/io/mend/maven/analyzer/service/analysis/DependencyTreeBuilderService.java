package io.mend.maven.analyzer.service.analysis;

import io.mend.maven.analyzer.config.MavenConstants;
import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import io.mend.maven.analyzer.service.hash.Sha1HashService;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class DependencyTreeBuilderService {
    private final Sha1HashService sha1HashService;
    
    public DependencyTreeBuilderService(@NonNull Sha1HashService sha1HashService) {
        this.sha1HashService = sha1HashService;
    }
    
    public List<AnalyzedDependency> buildDependencyTree(DependencyNode rootNode) {
        if (rootNode == null || rootNode.getChildren() == null) {
            return new ArrayList<>();
        }
        
        List<AnalyzedDependency> analyzedDependencies = new ArrayList<>();
        Set<String> visitedDependencyKeys = new HashSet<>();
        int[] progressCounter = new int[]{0};
        
        System.out.println("Processing dependencies:");
        
        for (DependencyNode child : rootNode.getChildren()) {
            AnalyzedDependency analyzed = convertToAnalyzedDependency(child, visitedDependencyKeys, progressCounter);
            if (analyzed != null) {
                analyzedDependencies.add(analyzed);
            }
        }
        
        return analyzedDependencies;
    }
    
    private AnalyzedDependency convertToAnalyzedDependency(DependencyNode dependencyNode, 
                                                           Set<String> visitedDependencyKeys, 
                                                           int[] progressCounter) {
        if (dependencyNode == null || dependencyNode.getDependency() == null) {
            return null;
        }
        
        Artifact artifact = dependencyNode.getDependency().getArtifact();
        String scope = dependencyNode.getDependency().getScope();
        
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        
        String uniqueDependencyKey = formatDependencyKey(groupId, artifactId, version, scope);
        
        if (visitedDependencyKeys.contains(uniqueDependencyKey)) {
            return null;
        }
        
        visitedDependencyKeys.add(uniqueDependencyKey);
        progressCounter[0]++;
        
        printDependencyProgress(progressCounter[0], groupId, artifactId, version, scope);
        
        AnalyzedDependency analyzedDependency = new AnalyzedDependency(groupId, artifactId, version, scope);
        
        String sha1Hash = sha1HashService.calculateSha1Hash(groupId, artifactId, version);
        analyzedDependency.setSha1(sha1Hash);
        
        printSha1Status(sha1Hash);
        
        List<AnalyzedDependency> transitiveDependencies = 
            processTransitiveDependencies(dependencyNode, visitedDependencyKeys, progressCounter);
        analyzedDependency.setChildren(transitiveDependencies);
        
        return analyzedDependency;
    }
    
    private List<AnalyzedDependency> processTransitiveDependencies(DependencyNode parentNode, 
                                                                   Set<String> visitedDependencyKeys, 
                                                                   int[] progressCounter) {
        if (parentNode.getChildren() == null || parentNode.getChildren().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<AnalyzedDependency> transitiveDependencies = new ArrayList<>();
        for (DependencyNode child : parentNode.getChildren()) {
            AnalyzedDependency analyzed = convertToAnalyzedDependency(child, visitedDependencyKeys, progressCounter);
            if (analyzed != null) {
                transitiveDependencies.add(analyzed);
            }
        }
        return transitiveDependencies;
    }
    
    private String formatDependencyKey(String groupId, String artifactId, String version, String scope) {
        return String.format(MavenConstants.COORDINATE_FORMAT, groupId, artifactId, version, scope);
    }
    
    private void printDependencyProgress(int count, String groupId, String artifactId, String version, String scope) {
        System.out.printf("  [%3d] %s:%s:%s (%s)", count, groupId, artifactId, version, scope);
    }
    
    private void printSha1Status(String sha1Hash) {
        if (sha1Hash != null && !sha1Hash.isEmpty()) {
            System.out.println(" ✓");
        } else {
            System.out.println(" [no SHA1]");
        }
    }
}