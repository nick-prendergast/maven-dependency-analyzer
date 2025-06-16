package io.mend.maven.analyzer.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"sha1", "children"})
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
    
    public AnalyzedDependency(@NonNull String groupId, @NonNull String artifactId, @NonNull String version, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
        this.children = new ArrayList<>();
    }
    
    public void addChild(@NonNull AnalyzedDependency child) {
        this.children.add(child);
    }
    
    @Override
    public String toString() {
        return String.format("%s:%s:%s:%s", groupId, artifactId, version, scope);
    }
}