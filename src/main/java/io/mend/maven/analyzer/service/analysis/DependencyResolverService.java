package io.mend.maven.analyzer.service.analysis;

import io.mend.maven.analyzer.config.MavenResolverConfig;
import io.mend.maven.analyzer.exception.DependencyAnalysisException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.util.ArrayList;
import java.util.List;

public class DependencyResolverService {
    
    private static final String DEFAULT_VERSION = "LATEST";
    private static final String DEFAULT_SCOPE = "compile";
    private static final String SCOPE_COMPILE = "compile";
    private static final String SCOPE_RUNTIME = "runtime";
    private static final String SCOPE_PROVIDED = "provided";
    private static final String SCOPE_TEST = "test";
    private static final String SCOPE_SYSTEM = "system";
    
    private final MavenResolverConfig config;
    
    public DependencyResolverService(MavenResolverConfig config) {
        this.config = config;
    }
    
    public DependencyNode resolveDependencies(Model model) throws DependencyAnalysisException {
        try {
            CollectRequest collectRequest = createCollectRequest(model);
            CollectResult collectResult = config.getRepositorySystem().collectDependencies(config.getSession(), collectRequest);
            
            DependencyRequest dependencyRequest = new DependencyRequest();
            dependencyRequest.setRoot(collectResult.getRoot());
            dependencyRequest.setFilter(DependencyFilterUtils.classpathFilter(SCOPE_COMPILE, SCOPE_RUNTIME, SCOPE_PROVIDED, SCOPE_TEST, SCOPE_SYSTEM));
            
            DependencyResult dependencyResult = config.getRepositorySystem().resolveDependencies(config.getSession(), dependencyRequest);
            
            return dependencyResult.getRoot();
            
        } catch (DependencyCollectionException e) {
            throw new DependencyAnalysisException("Failed to collect dependencies: " + e.getMessage(), e);
        } catch (DependencyResolutionException e) {
            throw new DependencyAnalysisException("Failed to resolve dependencies: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new DependencyAnalysisException("Unexpected error during dependency resolution: " + e.getMessage(), e);
        }
    }
    
    private CollectRequest createCollectRequest(Model model) {
        CollectRequest collectRequest = new CollectRequest();
        
        List<org.eclipse.aether.graph.Dependency> dependencies = new ArrayList<>();
        
        if (model.getDependencies() != null) {
            for (Dependency dependency : model.getDependencies()) {
                String coordinates = String.format("%s:%s:%s", 
                    dependency.getGroupId(), 
                    dependency.getArtifactId(), 
                    dependency.getVersion() != null ? dependency.getVersion() : DEFAULT_VERSION);
                
                Artifact artifact = new DefaultArtifact(coordinates);
                String scope = dependency.getScope() != null ? dependency.getScope() : DEFAULT_SCOPE;
                
                org.eclipse.aether.graph.Dependency aetherDependency = new org.eclipse.aether.graph.Dependency(artifact, scope);
                dependencies.add(aetherDependency);
            }
        }
        
        collectRequest.setDependencies(dependencies);
        collectRequest.setRepositories(config.getRepositories());
        
        return collectRequest;
    }
    
}