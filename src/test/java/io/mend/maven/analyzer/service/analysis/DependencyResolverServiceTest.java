package io.mend.maven.analyzer.service.analysis;

import io.mend.maven.analyzer.config.MavenResolverConfig;
import io.mend.maven.analyzer.exception.DependencyAnalysisException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DependencyResolverServiceTest {
    
    @Mock
    private MavenResolverConfig config;
    
    @Mock
    private RepositorySystem repositorySystem;
    
    @Mock
    private RepositorySystemSession session;
    
    @Mock
    private DependencyNode rootNode;
    
    
    private DependencyResolverService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DependencyResolverService(config);
        
        when(config.getRepositorySystem()).thenReturn(repositorySystem);
        when(config.getSession()).thenReturn(session);
        when(config.getRepositories()).thenReturn(Collections.emptyList());
    }
    
    @Test
    void testResolveDependencies_ValidModel_ReturnsRootNode() throws Exception {
        Model model = createSampleModel();
        CollectResult collectResult = mock(CollectResult.class);
        DependencyResult dependencyResult = mock(DependencyResult.class);
        
        when(repositorySystem.collectDependencies(eq(session), any(CollectRequest.class)))
            .thenReturn(collectResult);
        when(collectResult.getRoot()).thenReturn(rootNode);
        when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest.class)))
            .thenReturn(dependencyResult);
        when(dependencyResult.getRoot()).thenReturn(rootNode);
        
        DependencyNode result = service.resolveDependencies(model);
        
        assertNotNull(result);
        assertEquals(rootNode, result);
        verify(repositorySystem).collectDependencies(eq(session), any(CollectRequest.class));
        verify(repositorySystem).resolveDependencies(eq(session), any(DependencyRequest.class));
    }
    
    @Test
    void testResolveDependencies_CollectionException_ThrowsDependencyAnalysisException() throws Exception {
        Model model = createSampleModel();
        
        when(repositorySystem.collectDependencies(eq(session), any(CollectRequest.class)))
            .thenThrow(new RuntimeException("Collection failed"));
        
        DependencyAnalysisException exception = assertThrows(DependencyAnalysisException.class,
            () -> service.resolveDependencies(model));
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }
    
    @Test
    void testResolveDependencies_ResolutionException_ThrowsDependencyAnalysisException() throws Exception {
        Model model = createSampleModel();
        CollectResult collectResult = mock(CollectResult.class);
        
        when(repositorySystem.collectDependencies(eq(session), any(CollectRequest.class)))
            .thenReturn(collectResult);
        when(collectResult.getRoot()).thenReturn(rootNode);
        when(repositorySystem.resolveDependencies(eq(session), any(DependencyRequest.class)))
            .thenThrow(new RuntimeException("Resolution failed"));
        
        DependencyAnalysisException exception = assertThrows(DependencyAnalysisException.class,
            () -> service.resolveDependencies(model));
        
        assertNotNull(exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }
    
    
    private Model createSampleModel() {
        Model model = new Model();
        model.setGroupId("com.test");
        model.setArtifactId("test-project");
        model.setVersion("1.0.0");
        
        Dependency dependency = new Dependency();
        dependency.setGroupId("org.junit.jupiter");
        dependency.setArtifactId("junit-jupiter-api");
        dependency.setVersion("5.8.2");
        dependency.setScope("test");
        
        model.setDependencies(Arrays.asList(dependency));
        
        return model;
    }
}