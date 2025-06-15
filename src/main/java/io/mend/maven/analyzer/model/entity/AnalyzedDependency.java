package io.mend.maven.analyzer.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyzedDependency {
    
    @JsonProperty("groupId")
    private String groupId;
    
    @JsonProperty("artifactId")
    private String artifactId;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("sha1")
    private String sha1;
    
    @JsonProperty("scope")
    private String scope;
    
    @JsonProperty("children")
    private List<AnalyzedDependency> children = new ArrayList<>();
    
    public AnalyzedDependency() {
        // children already initialized above
    }
    
    public AnalyzedDependency(String groupId, String artifactId, String version, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
        // children already initialized above
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getSha1() {
        return sha1;
    }
    
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public List<AnalyzedDependency> getChildren() {
        return children;
    }
    
    public void setChildren(List<AnalyzedDependency> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
    
    public void addChild(AnalyzedDependency child) {
        this.children.add(child);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalyzedDependency that = (AnalyzedDependency) o;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(artifactId, that.artifactId) &&
               Objects.equals(version, that.version) &&
               Objects.equals(scope, that.scope);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, scope);
    }
    
    @Override
    public String toString() {
        return String.format("%s:%s:%s:%s", groupId, artifactId, version, scope);
    }
    
    @JsonIgnore
    public String getCoordinates() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }
}