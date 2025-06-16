package io.mend.maven.analyzer.util;

import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Utility class for common dependency operations.
 */
@UtilityClass
public class DependencyUtils {
    
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