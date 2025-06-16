package io.mend.maven.analyzer.service.output;

import io.mend.maven.analyzer.TestConstants;
import io.mend.maven.analyzer.model.entity.AnalyzedDependency;
import io.mend.maven.analyzer.model.response.AnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonOutputServiceTest {
    
    private JsonOutputService service;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        service = new JsonOutputService();
    }
    
    @Test
    void testWriteToFile_ValidResult_CreatesJsonFile() throws IOException {
        AnalysisResult result = createSampleResult();
        Path outputFile = tempDir.resolve("output.json");
        
        service.writeToFile(result, outputFile.toString());
        
        assertTrue(Files.exists(outputFile));
        String content = Files.readString(outputFile);
        assertTrue(content.contains("\"projectGroupId\" : \"" + TestConstants.TEST_PROJECT_GROUP_ID + "\""));
        assertTrue(content.contains("\"projectArtifactId\" : \"" + TestConstants.TEST_PROJECT_ARTIFACT_ID + "\""));
    }
    
    @Test
    void testWriteToFile_CreatesDirectories_WhenParentNotExists() throws IOException {
        AnalysisResult result = createSampleResult();
        Path outputFile = tempDir.resolve("nested/dir/output.json");
        
        service.writeToFile(result, outputFile.toString());
        
        assertTrue(Files.exists(outputFile));
        assertTrue(Files.exists(outputFile.getParent()));
    }
    
    @Test
    void testWriteToFile_NullPath_ThrowsException() {
        AnalysisResult result = createSampleResult();
        
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> service.writeToFile(result, null));
        
        assertEquals("outputPath is marked non-null but is null", exception.getMessage());
    }
    
    @Test
    void testWriteToFile_EmptyPath_ThrowsException() {
        AnalysisResult result = createSampleResult();
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> service.writeToFile(result, ""));
        
        assertEquals("Output path cannot be empty", exception.getMessage());
    }
    
    @Test
    void testWriteToFile_ValidResult_ProducesValidJson() throws IOException {
        AnalysisResult result = createSampleResult();
        Path outputFile = tempDir.resolve("output.json");
        
        service.writeToFile(result, outputFile.toString());
        
        assertTrue(Files.exists(outputFile));
        String content = Files.readString(outputFile);
        
        // Verify JSON contains expected fields and values
        assertTrue(content.contains("\"projectGroupId\" : \"" + TestConstants.TEST_PROJECT_GROUP_ID + "\""));
        assertTrue(content.contains("\"projectArtifactId\" : \"" + TestConstants.TEST_PROJECT_ARTIFACT_ID + "\""));
        assertTrue(content.contains("\"projectVersion\" : \"" + TestConstants.TEST_PROJECT_VERSION + "\""));
        assertTrue(content.contains("\"dependencies\""));
        assertTrue(content.contains("\"totalDependencies\""));
        assertTrue(content.contains(TestConstants.TEST_DEPENDENCY_GROUP_ID));
        assertTrue(content.contains(TestConstants.TEST_DEPENDENCY_ARTIFACT_ID));
        assertTrue(content.contains(TestConstants.SAMPLE_SHA1_HASH));
    }
    
    private AnalysisResult createSampleResult() {
        AnalysisResult result = new AnalysisResult(
            "/test/path",
            TestConstants.TEST_PROJECT_GROUP_ID,
            TestConstants.TEST_PROJECT_ARTIFACT_ID,
            TestConstants.TEST_PROJECT_VERSION
        );
        
        AnalyzedDependency dependency = new AnalyzedDependency(
            TestConstants.TEST_DEPENDENCY_GROUP_ID,
            TestConstants.TEST_DEPENDENCY_ARTIFACT_ID,
            TestConstants.TEST_DEPENDENCY_VERSION,
            TestConstants.TEST_DEPENDENCY_SCOPE
        );
        dependency.setSha1(TestConstants.SAMPLE_SHA1_HASH);
        
        List<AnalyzedDependency> dependencies = Arrays.asList(dependency);
        result.setDependencies(dependencies);
        
        return result;
    }
}