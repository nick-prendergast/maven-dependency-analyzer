package io.mend.maven.analyzer.service.analysis;

import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import io.mend.maven.analyzer.service.hash.Sha1HashService;
import io.mend.maven.analyzer.util.DependencyUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DependencyTreeBuilderServiceTest {
    
    @Mock
    private Sha1HashService sha1HashService;
    
    @Mock
    private DependencyNode aetherRoot;
    
    @Mock
    private DependencyNode aetherChild;
    
    @Mock
    private DependencyNode aetherGrandchild;
    
    @Mock
    private Dependency dependency;
    
    @Mock
    private Dependency childDependency;
    
    @Mock
    private Artifact artifact;
    
    @Mock
    private Artifact childArtifact;
    
    private DependencyTreeBuilderService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DependencyTreeBuilderService(sha1HashService);
    }
    
    @Test
    void testBuildDependencyTree_WithChildren_ReturnsCorrectTree() {
        // Setup mocks
        setupMockDependency("com.example", "parent", "1.0.0", "compile");
        setupMockChildDependency("org.junit", "junit", "4.13.2", "test");
        
        when(aetherRoot.getChildren()).thenReturn(Arrays.asList(aetherChild));
        when(aetherChild.getDependency()).thenReturn(dependency);
        when(aetherChild.getChildren()).thenReturn(Collections.emptyList());
        
        when(sha1HashService.calculateSha1Hash("com.example", "parent", "1.0.0"))
            .thenReturn("abc123");
        
        List<AnalyzedDependency> result = service.buildDependencyTree(aetherRoot);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        
        AnalyzedDependency rootDep = result.get(0);
        assertEquals("com.example", rootDep.getGroupId());
        assertEquals("parent", rootDep.getArtifactId());
        assertEquals("1.0.0", rootDep.getVersion());
        assertEquals("compile", rootDep.getScope());
        assertEquals("abc123", rootDep.getSha1());
        assertTrue(rootDep.getChildren().isEmpty());
    }
    
    @Test
    void testBuildDependencyTree_WithNestedChildren_ReturnsNestedTree() {
        // Setup nested structure
        setupMockDependency("com.example", "parent", "1.0.0", "compile");
        setupMockChildDependency("org.junit", "junit", "4.13.2", "test");
        
        when(aetherRoot.getChildren()).thenReturn(Arrays.asList(aetherChild));
        when(aetherChild.getDependency()).thenReturn(dependency);
        when(aetherChild.getChildren()).thenReturn(Arrays.asList(aetherGrandchild));
        
        // Setup grandchild
        Dependency grandchildDep = mock(Dependency.class);
        Artifact grandchildArtifact = mock(Artifact.class);
        when(aetherGrandchild.getDependency()).thenReturn(grandchildDep);
        when(grandchildDep.getArtifact()).thenReturn(grandchildArtifact);
        when(grandchildDep.getScope()).thenReturn("runtime");
        when(grandchildArtifact.getGroupId()).thenReturn("org.slf4j");
        when(grandchildArtifact.getArtifactId()).thenReturn("slf4j-api");
        when(grandchildArtifact.getVersion()).thenReturn("1.7.36");
        when(aetherGrandchild.getChildren()).thenReturn(Collections.emptyList());
        
        when(sha1HashService.calculateSha1Hash(anyString(), anyString(), anyString()))
            .thenReturn("hash123");
        
        List<AnalyzedDependency> result = service.buildDependencyTree(aetherRoot);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        
        AnalyzedDependency rootDep = result.get(0);
        assertEquals(1, rootDep.getChildren().size());
        
        AnalyzedDependency childDep = rootDep.getChildren().get(0);
        assertEquals("org.slf4j", childDep.getGroupId());
        assertEquals("slf4j-api", childDep.getArtifactId());
        assertEquals("runtime", childDep.getScope());
    }
    
    @Test
    void testBuildDependencyTree_NullRoot_ReturnsEmptyList() {
        List<AnalyzedDependency> result = service.buildDependencyTree(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testBuildDependencyTree_NullChildren_ReturnsEmptyList() {
        when(aetherRoot.getChildren()).thenReturn(null);
        
        List<AnalyzedDependency> result = service.buildDependencyTree(aetherRoot);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testBuildDependencyTree_DuplicateDependencies_NoDuplicatesInResult() {
        // Setup two identical dependencies
        setupMockDependency("com.example", "duplicate", "1.0.0", "compile");
        
        DependencyNode aetherChild2 = mock(DependencyNode.class);
        when(aetherChild2.getDependency()).thenReturn(dependency);
        when(aetherChild2.getChildren()).thenReturn(Collections.emptyList());
        
        when(aetherRoot.getChildren()).thenReturn(Arrays.asList(aetherChild, aetherChild2));
        when(aetherChild.getDependency()).thenReturn(dependency);
        when(aetherChild.getChildren()).thenReturn(Collections.emptyList());
        
        when(sha1HashService.calculateSha1Hash("com.example", "duplicate", "1.0.0"))
            .thenReturn("hash123");
        
        List<AnalyzedDependency> result = service.buildDependencyTree(aetherRoot);
        
        assertNotNull(result);
        assertEquals(1, result.size()); // Should only have one instance due to duplicate detection
    }
    
    @Test
    void testCountTotalDependencies_WithNestedTree_ReturnsCorrectCount() {
        AnalyzedDependency parent = new AnalyzedDependency("com.example", "parent", "1.0.0", "compile");
        AnalyzedDependency child1 = new AnalyzedDependency("org.junit", "junit", "4.13.2", "test");
        AnalyzedDependency child2 = new AnalyzedDependency("org.slf4j", "slf4j-api", "1.7.36", "compile");
        AnalyzedDependency grandchild = new AnalyzedDependency("org.hamcrest", "hamcrest", "2.2", "test");
        
        child1.addChild(grandchild);
        parent.setChildren(Arrays.asList(child1, child2));
        
        int count = DependencyUtils.countTotalDependencies(Arrays.asList(parent));
        
        assertEquals(4, count); // parent + child1 + child2 + grandchild
    }
    
    @Test
    void testCountTotalDependencies_EmptyList_ReturnsZero() {
        int count = DependencyUtils.countTotalDependencies(Collections.emptyList());
        
        assertEquals(0, count);
    }
    
    
    private void setupMockDependency(String groupId, String artifactId, String version, String scope) {
        when(dependency.getArtifact()).thenReturn(artifact);
        when(dependency.getScope()).thenReturn(scope);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getVersion()).thenReturn(version);
    }
    
    private void setupMockChildDependency(String groupId, String artifactId, String version, String scope) {
        when(childDependency.getArtifact()).thenReturn(childArtifact);
        when(childDependency.getScope()).thenReturn(scope);
        when(childArtifact.getGroupId()).thenReturn(groupId);
        when(childArtifact.getArtifactId()).thenReturn(artifactId);
        when(childArtifact.getVersion()).thenReturn(version);
    }
}