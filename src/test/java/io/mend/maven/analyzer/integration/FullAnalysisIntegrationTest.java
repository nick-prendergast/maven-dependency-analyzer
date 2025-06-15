package io.mend.maven.analyzer.integration;

import io.mend.maven.analyzer.TestConstants;
import io.mend.maven.analyzer.MavenDependencyAnalyzerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FullAnalysisIntegrationTest {
    
    private MavenDependencyAnalyzerApplication application;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        application = new MavenDependencyAnalyzerApplication();
        
        // Capture output for testing
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));
    }
    
    void tearDown() {
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    
    @Test
    void testFullAnalysis_ValidProject_GeneratesJsonOutput() throws Exception {
        // Create a test Maven project
        Path projectDir = tempDir.resolve("test-project");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("pom.xml"), TestConstants.SAMPLE_POM_XML);
        
        Path outputFile = tempDir.resolve("output.json");
        
        String[] args = {"-d", projectDir.toString(), "-o", outputFile.toString()};
        
        try {
            application.run(args);
            
            // Verify output file was created
            assertTrue(Files.exists(outputFile));
            
            // Verify JSON content
            String jsonContent = Files.readString(outputFile);
            assertTrue(jsonContent.contains("\"projectGroupId\""));
            assertTrue(jsonContent.contains("\"dependencies\""));
            assertTrue(jsonContent.contains("\"totalDependencies\""));
            
            // Verify console output
            String output = outputStream.toString();
            assertTrue(output.contains("✓ Analysis completed successfully!"));
            assertTrue(output.contains("Total dependencies:"));
            
        } finally {
            tearDown();
        }
    }
    
    @Test
    void testFullAnalysis_EmptyProject_GeneratesEmptyDependencyList() throws Exception {
        // Create a test Maven project without dependencies
        Path projectDir = tempDir.resolve("empty-project");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("pom.xml"), TestConstants.EMPTY_POM_XML);
        
        Path outputFile = tempDir.resolve("empty-output.json");
        
        String[] args = {"-d", projectDir.toString(), "-o", outputFile.toString()};
        
        try {
            application.run(args);
            
            // Verify output file was created
            assertTrue(Files.exists(outputFile));
            
            // Verify JSON content for empty project
            String jsonContent = Files.readString(outputFile);
            assertTrue(jsonContent.contains("\"totalDependencies\" : 0"));
            assertTrue(jsonContent.contains("\"dependencies\" : [ ]"));
            
        } finally {
            tearDown();
        }
    }
    
    @Test
    void testFullAnalysis_HelpFlag_DisplaysHelp() throws Exception {
        String[] args = {"--help"};
        
        try {
            application.run(args);
            
            String output = outputStream.toString();
            assertTrue(output.contains("usage: maven-dependency-analyzer"));
            assertTrue(output.contains("-d,--directory"));
            assertTrue(output.contains("-o,--output"));
            assertTrue(output.contains("--help"));
            
        } finally {
            tearDown();
        }
    }
    
    @Test
    void testFullAnalysis_InvalidProject_ShowsError() throws Exception {
        Path nonExistentPath = tempDir.resolve("non-existent");
        Path outputFile = tempDir.resolve("output.json");
        
        String[] args = {"-d", nonExistentPath.toString(), "-o", outputFile.toString()};
        
        try {
            Exception exception = assertThrows(Exception.class, () -> application.run(args));
            
            // Verify error is properly handled
            assertNotNull(exception);
            
        } finally {
            tearDown();
        }
    }
    
    @Test
    void testFullAnalysis_MissingArguments_ShowsError() throws Exception {
        String[] args = {}; // Empty arguments
        
        try {
            Exception exception = assertThrows(Exception.class, () -> application.run(args));
            
            // Verify error is properly handled
            assertNotNull(exception);
            
        } finally {
            tearDown();
        }
    }
    
    @Test
    void testFullAnalysis_InvalidPom_ShowsError() throws Exception {
        // Create a project with invalid pom.xml
        Path projectDir = tempDir.resolve("invalid-project");
        Files.createDirectories(projectDir);
        Files.writeString(projectDir.resolve("pom.xml"), TestConstants.INVALID_POM_XML);
        
        Path outputFile = tempDir.resolve("output.json");
        
        String[] args = {"-d", projectDir.toString(), "-o", outputFile.toString()};
        
        try {
            Exception exception = assertThrows(Exception.class, () -> application.run(args));
            
            // Verify error is properly handled
            assertNotNull(exception);
            
        } finally {
            tearDown();
        }
    }
}