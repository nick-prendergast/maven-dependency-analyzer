package io.mend.maven.analyzer.service.analysis;

import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import io.mend.maven.analyzer.service.hash.Sha1HashService;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyTreeBuilderService {
    
    private final Sha1HashService sha1HashService;
    private int dependencyCount = 0;
    
    public DependencyTreeBuilderService(Sha1HashService sha1HashService) {
        this.sha1HashService = sha1HashService;
    }
    
    public List<AnalyzedDependency> buildDependencyTree(DependencyNode aetherRoot) {
        if (aetherRoot == null || aetherRoot.getChildren() == null) {
            return new ArrayList<>();
        }
        
        List<AnalyzedDependency> rootNodes = new ArrayList<>();
        Set<String> processedDependencies = new HashSet<>();
        dependencyCount = 0;
        
        System.out.println("Processing dependencies:");
        
        for (DependencyNode aetherChild : aetherRoot.getChildren()) {
            AnalyzedDependency node = convertAetherNode(aetherChild, processedDependencies);
            if (node != null) {
                rootNodes.add(node);
            }
        }
        
        return rootNodes;
    }
    
    private AnalyzedDependency convertAetherNode(DependencyNode aetherNode, Set<String> processedDependencies) {
        if (aetherNode == null || aetherNode.getDependency() == null) {
            return null;
        }
        
        Artifact artifact = aetherNode.getDependency().getArtifact();
        String scope = aetherNode.getDependency().getScope();
        
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();
        
        String dependencyKey = String.format("%s:%s:%s:%s", groupId, artifactId, version, scope);
        
        if (processedDependencies.contains(dependencyKey)) {
            return null;
        }
        processedDependencies.add(dependencyKey);
        
        // Show progress
        dependencyCount++;
        System.out.printf("  [%3d] %s:%s:%s (%s)", 
            dependencyCount, groupId, artifactId, version, scope);
        
        AnalyzedDependency node = new AnalyzedDependency(groupId, artifactId, version, scope);
        
        String sha1Hash = sha1HashService.calculateSha1Hash(groupId, artifactId, version);
        node.setSha1(sha1Hash);
        
        if (sha1Hash != null && !sha1Hash.isEmpty()) {
            System.out.println(" ✓");
        } else {
            System.out.println(" [no SHA1]");
        }
        
        List<AnalyzedDependency> children = processChildren(aetherNode, processedDependencies);
        node.setChildren(children);
        
        return node;
    }
    
    /**
     * Processes child dependencies of an Aether node.
     */
    private List<AnalyzedDependency> processChildren(DependencyNode aetherNode, Set<String> processedDependencies) {
        if (aetherNode.getChildren() == null || aetherNode.getChildren().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<AnalyzedDependency> children = new ArrayList<>();
        for (DependencyNode aetherChild : aetherNode.getChildren()) {
            AnalyzedDependency childNode = convertAetherNode(aetherChild, processedDependencies);
            if (childNode != null) {
                children.add(childNode);
            }
        }
        return children;
    }
    
}