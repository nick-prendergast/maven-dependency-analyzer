package io.mend.maven.analyzer.service.detection;

import io.mend.maven.analyzer.TestConstants;
import io.mend.maven.analyzer.exception.MavenProjectException;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MavenProjectDetectionServiceTest {
    
    private MavenProjectDetectionService service;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        service = new MavenProjectDetectionService();
    }
    
    
    @Test
    void testReadPomModel_ValidProject_ReturnsModel() throws IOException, MavenProjectException {
        // Create a valid Maven project
        Path projectDir = tempDir.resolve("valid-project");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("pom.xml"), TestConstants.SAMPLE_POM_XML);
        
        Model model = service.readPomModel(projectDir.toString());
        
        assertNotNull(model);
        assertEquals(TestConstants.TEST_PROJECT_GROUP_ID, model.getGroupId());
        assertEquals(TestConstants.TEST_PROJECT_ARTIFACT_ID, model.getArtifactId());
        assertEquals(TestConstants.TEST_PROJECT_VERSION, model.getVersion());
        assertNotNull(model.getDependencies());
        assertEquals(1, model.getDependencies().size());
    }
    
    @Test
    void testReadPomModel_EmptyProject_ReturnsModelWithoutDependencies() throws IOException, MavenProjectException {
        // Create a valid Maven project without dependencies
        Path projectDir = tempDir.resolve("empty-project");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("pom.xml"), TestConstants.EMPTY_POM_XML);
        
        Model model = service.readPomModel(projectDir.toString());
        
        assertNotNull(model);
        assertEquals(TestConstants.TEST_PROJECT_GROUP_ID, model.getGroupId());
        assertEquals(TestConstants.TEST_PROJECT_ARTIFACT_ID, model.getArtifactId());
        assertEquals(TestConstants.TEST_PROJECT_VERSION, model.getVersion());
        assertTrue(model.getDependencies() == null || model.getDependencies().isEmpty());
    }
    
    @Test
    void testGetEffectiveGroupId_DirectGroupId_ReturnsGroupId() throws IOException, MavenProjectException {
        // Create a valid Maven project
        Path projectDir = tempDir.resolve("valid-project");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("pom.xml"), TestConstants.SAMPLE_POM_XML);
        
        Model model = service.readPomModel(projectDir.toString());
        String groupId = service.getEffectiveGroupId(model);
        
        assertEquals(TestConstants.TEST_PROJECT_GROUP_ID, groupId);
    }
    
    @Test
    void testGetEffectiveVersion_DirectVersion_ReturnsVersion() throws IOException, MavenProjectException {
        // Create a valid Maven project
        Path projectDir = tempDir.resolve("valid-project");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("pom.xml"), TestConstants.SAMPLE_POM_XML);
        
        Model model = service.readPomModel(projectDir.toString());
        String version = service.getEffectiveVersion(model);
        
        assertEquals(TestConstants.TEST_PROJECT_VERSION, version);
    }
}