package io.mend.maven.analyzer.service.detection;

import org.apache.maven.model.Model;

/**
 * Extracts effective properties from Maven Model objects.
 * 
 * This service handles the extraction of effective Maven properties from Model objects,
 * implementing Maven's inheritance logic where properties can be inherited from parent
 * POMs when not explicitly defined in the current project.
 */
public class ModelPropertyExtractor {
    
    /**
     * Gets the effective groupId, falling back to parent if not set directly.
     */
    public String getEffectiveGroupId(Model model) {
        if (model.getGroupId() != null) {
            return model.getGroupId();
        }
        if (model.getParent() != null && model.getParent().getGroupId() != null) {
            return model.getParent().getGroupId();
        }
        return null;
    }
    
    /**
     * Gets the effective version, falling back to parent if not set directly.
     */
    public String getEffectiveVersion(Model model) {
        if (model.getVersion() != null) {
            return model.getVersion();
        }
        if (model.getParent() != null && model.getParent().getVersion() != null) {
            return model.getParent().getVersion();
        }
        return null;
    }
}