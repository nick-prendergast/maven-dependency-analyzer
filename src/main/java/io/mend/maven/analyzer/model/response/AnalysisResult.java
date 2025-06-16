package io.mend.maven.analyzer.model.response;

import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import io.mend.maven.analyzer.util.DependencyUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResult {
    
    @JsonProperty("projectPath")
    private String projectPath;
    
    @JsonProperty("projectGroupId")
    private String projectGroupId;
    
    @JsonProperty("projectArtifactId")
    private String projectArtifactId;
    
    @JsonProperty("projectVersion")
    private String projectVersion;
    
    @JsonProperty("dependencies")
    private List<AnalyzedDependency> dependencies;
    
    @JsonProperty("totalDependencies")
    private int totalDependencies;
    
    public AnalysisResult(@NonNull String projectPath, @NonNull String projectGroupId, @NonNull String projectArtifactId, @NonNull String projectVersion) {
        this.projectPath = projectPath;
        this.projectGroupId = projectGroupId;
        this.projectArtifactId = projectArtifactId;
        this.projectVersion = projectVersion;
    }
    
    public void setDependencies(@NonNull List<AnalyzedDependency> dependencies) {
        this.dependencies = dependencies;
        this.totalDependencies = DependencyUtils.countTotalDependencies(dependencies);
    }
}