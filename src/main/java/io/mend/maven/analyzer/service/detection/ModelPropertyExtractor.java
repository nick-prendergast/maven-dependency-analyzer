package io.mend.maven.analyzer.service.detection;

import lombok.NonNull;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import java.util.function.Function;

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
    public String getEffectiveGroupId(@NonNull Model model) {
        return getEffectiveProperty(model, Model::getGroupId, Parent::getGroupId);
    }
    
    /**
     * Gets the effective version, falling back to parent if not set directly.
     */
    public String getEffectiveVersion(@NonNull Model model) {
        return getEffectiveProperty(model, Model::getVersion, Parent::getVersion);
    }
    
    private String getEffectiveProperty(@NonNull Model model, 
                                        @NonNull Function<Model, String> modelGetter,
                                        @NonNull Function<Parent, String> parentGetter) {
        String value = modelGetter.apply(model);
        if (value != null) {
            return value;
        }
        
        Parent parent = model.getParent();
        if (parent != null) {
            return parentGetter.apply(parent);
        }
        
        return null;
    }
}