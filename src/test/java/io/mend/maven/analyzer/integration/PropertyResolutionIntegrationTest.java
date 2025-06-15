package io.mend.maven.analyzer.integration;

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

/**
 * Integration test for property resolution in the complete dependency analysis pipeline.
 */
class PropertyResolutionIntegrationTest {
    
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
    void testPropertyResolution_TextProcessorLikePOM_SucceedsWithoutErrors() throws Exception {
        // Create a test project that mimics the text-processor issue
        Path projectDir = tempDir.resolve("property-test-project");
        Files.createDirectories(projectDir);
        
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.betvictor</groupId>
                <artifactId>property-test</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <name>Property Resolution Test</name>
                
                <properties>
                    <java.version>21</java.version>
                    <mockwebserver.version>4.12.0</mockwebserver.version>
                    <awaitility.version>4.2.2</awaitility.version>
                    <testcontainers.version>1.19.3</testcontainers.version>
                </properties>
                
                <dependencies>
                    <!-- Core Spring Boot (no version, managed by parent) -->
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>
                    
                    <!-- Testing dependencies with properties -->
                    <dependency>
                        <groupId>com.squareup.okhttp3</groupId>
                        <artifactId>mockwebserver</artifactId>
                        <version>${mockwebserver.version}</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.awaitility</groupId>
                        <artifactId>awaitility</artifactId>
                        <version>${awaitility.version}</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
                
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>org.testcontainers</groupId>
                            <artifactId>testcontainers-bom</artifactId>
                            <version>${testcontainers.version}</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
            </project>
            """;
        
        Files.writeString(projectDir.resolve("pom.xml"), pomContent);
        Path outputFile = tempDir.resolve("property-test-output.json");
        
        String[] args = {"-d", projectDir.toString(), "-o", outputFile.toString()};
        
        try {
            application.run(args);
            
            // Verify output file was created
            assertTrue(Files.exists(outputFile), "Output file should be created");
            
            // Verify JSON content contains resolved properties
            String jsonContent = Files.readString(outputFile);
            assertFalse(jsonContent.contains("${mockwebserver.version}"), 
                "JSON should not contain unresolved mockwebserver.version property");
            assertFalse(jsonContent.contains("${awaitility.version}"), 
                "JSON should not contain unresolved awaitility.version property");
            assertFalse(jsonContent.contains("${testcontainers.version}"), 
                "JSON should not contain unresolved testcontainers.version property");
            
            // Should contain resolved versions
            assertTrue(jsonContent.contains("4.12.0"), 
                "JSON should contain resolved mockwebserver version 4.12.0");
            assertTrue(jsonContent.contains("4.2.2"), 
                "JSON should contain resolved awaitility version 4.2.2");
            
            // Verify basic JSON structure
            assertTrue(jsonContent.contains("\"projectGroupId\""));
            assertTrue(jsonContent.contains("\"projectArtifactId\""));
            assertTrue(jsonContent.contains("\"dependencies\""));
            assertTrue(jsonContent.contains("\"totalDependencies\""));
            
            // Verify console output indicates success
            String output = outputStream.toString();
            assertTrue(output.contains("Analysis completed successfully"), 
                "Should show successful completion message");
            assertFalse(output.contains("Failed to resolve dependencies"), 
                "Should not show dependency resolution errors");
            
        } finally {
            tearDown();
        }
    }
    
    @Test
    void testPropertyResolution_SimplePropertiesProject_ResolvesCorrectly() throws Exception {
        // Create a simpler test project with basic properties
        Path projectDir = tempDir.resolve("simple-props-project");
        Files.createDirectories(projectDir);
        
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>simple-props-test</artifactId>
                <version>1.0.0</version>
                
                <properties>
                    <junit.version>5.8.2</junit.version>
                    <mockito.version>4.6.1</mockito.version>
                </properties>
                
                <dependencies>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter-api</artifactId>
                        <version>${junit.version}</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.mockito</groupId>
                        <artifactId>mockito-core</artifactId>
                        <version>${mockito.version}</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """;
        
        Files.writeString(projectDir.resolve("pom.xml"), pomContent);
        Path outputFile = tempDir.resolve("simple-props-output.json");
        
        String[] args = {"-d", projectDir.toString(), "-o", outputFile.toString()};
        
        try {
            application.run(args);
            
            // Verify output file was created
            assertTrue(Files.exists(outputFile));
            
            // Verify properties were resolved
            String jsonContent = Files.readString(outputFile);
            assertFalse(jsonContent.contains("${junit.version}"));
            assertFalse(jsonContent.contains("${mockito.version}"));
            assertTrue(jsonContent.contains("5.8.2"));
            assertTrue(jsonContent.contains("4.6.1"));
            
            // Verify no dependency resolution errors
            String output = outputStream.toString();
            assertFalse(output.contains("Failed to resolve dependencies"));
            assertTrue(output.contains("Analysis completed successfully"));
            
        } finally {
            tearDown();
        }
    }
    
    @Test
    void testPropertyResolution_MultipleProperties_ResolvesAllCorrectly() throws Exception {
        // Test with multiple properties all being resolved correctly
        Path projectDir = tempDir.resolve("multi-props-project");
        Files.createDirectories(projectDir);
        
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>multi-props-test</artifactId>
                <version>1.0.0</version>
                
                <properties>
                    <junit.version>5.8.2</junit.version>
                    <mockito.version>4.6.1</mockito.version>
                    <commons.version>3.12.0</commons.version>
                </properties>
                
                <dependencies>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter-api</artifactId>
                        <version>${junit.version}</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.mockito</groupId>
                        <artifactId>mockito-core</artifactId>
                        <version>${mockito.version}</version>
                        <scope>test</scope>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.commons</groupId>
                        <artifactId>commons-lang3</artifactId>
                        <version>${commons.version}</version>
                    </dependency>
                </dependencies>
            </project>
            """;
        
        Files.writeString(projectDir.resolve("pom.xml"), pomContent);
        Path outputFile = tempDir.resolve("multi-props-output.json");
        
        String[] args = {"-d", projectDir.toString(), "-o", outputFile.toString()};
        
        try {
            application.run(args);
            
            // Verify output file was created
            assertTrue(Files.exists(outputFile));
            
            String jsonContent = Files.readString(outputFile);
            
            // All properties should be resolved
            assertFalse(jsonContent.contains("${junit.version}"));
            assertFalse(jsonContent.contains("${mockito.version}"));
            assertFalse(jsonContent.contains("${commons.version}"));
            
            // Should contain resolved versions
            assertTrue(jsonContent.contains("5.8.2"));
            assertTrue(jsonContent.contains("4.6.1"));
            assertTrue(jsonContent.contains("3.12.0"));
            
            // Should complete successfully
            String output = outputStream.toString();
            assertTrue(output.contains("Analysis completed successfully"));
            
        } finally {
            tearDown();
        }
    }
}