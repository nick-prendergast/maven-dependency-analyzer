package io.mend.maven.analyzer.model.response;

import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import io.mend.maven.analyzer.util.DependencyUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
    
    public AnalysisResult() {
    }
    
    public AnalysisResult(String projectPath, String projectGroupId, String projectArtifactId, String projectVersion) {
        this.projectPath = projectPath;
        this.projectGroupId = projectGroupId;
        this.projectArtifactId = projectArtifactId;
        this.projectVersion = projectVersion;
    }
    
    public String getProjectPath() {
        return projectPath;
    }
    
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }
    
    public String getProjectGroupId() {
        return projectGroupId;
    }
    
    public void setProjectGroupId(String projectGroupId) {
        this.projectGroupId = projectGroupId;
    }
    
    public String getProjectArtifactId() {
        return projectArtifactId;
    }
    
    public void setProjectArtifactId(String projectArtifactId) {
        this.projectArtifactId = projectArtifactId;
    }
    
    public String getProjectVersion() {
        return projectVersion;
    }
    
    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }
    
    public List<AnalyzedDependency> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<AnalyzedDependency> dependencies) {
        this.dependencies = dependencies;
        if (dependencies != null) {
            this.totalDependencies = DependencyUtils.countTotalDependencies(dependencies);
        }
    }
    
    public int getTotalDependencies() {
        return totalDependencies;
    }
}