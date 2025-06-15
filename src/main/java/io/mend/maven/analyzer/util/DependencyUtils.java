package io.mend.maven.analyzer.util;

import io.mend.maven.analyzer.model.entity.AnalyzedDependency;

import java.util.List;

/**
 * Utility class for common dependency operations.
 */
public final class DependencyUtils {
    
    private DependencyUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Counts the total number of dependencies recursively.
     */
    public static int countTotalDependencies(List<AnalyzedDependency> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }
        
        int count = nodes.size();
        for (AnalyzedDependency node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                count += countTotalDependencies(node.getChildren());
            }
        }
        return count;
    }
}