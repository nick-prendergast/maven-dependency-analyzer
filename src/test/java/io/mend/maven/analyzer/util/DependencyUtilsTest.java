package io.mend.maven.analyzer.util;

import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DependencyUtilsTest {

    @Test
    void testCountTotalDependencies_WithNestedDependencies() {
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
    
    @Test
    void testCountTotalDependencies_NullList_ReturnsZero() {
        int count = DependencyUtils.countTotalDependencies(null);
        
        assertEquals(0, count);
    }
    
    @Test
    void testCountTotalDependencies_FlatList() {
        AnalyzedDependency dep1 = new AnalyzedDependency("org.junit", "junit", "4.13.2", "test");
        AnalyzedDependency dep2 = new AnalyzedDependency("org.slf4j", "slf4j-api", "1.7.36", "compile");
        
        int count = DependencyUtils.countTotalDependencies(Arrays.asList(dep1, dep2));
        
        assertEquals(2, count);
    }
}