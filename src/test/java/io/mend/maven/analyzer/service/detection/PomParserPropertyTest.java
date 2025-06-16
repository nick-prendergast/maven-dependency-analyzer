package io.mend.maven.analyzer.service.detection;

import io.mend.maven.analyzer.exception.MavenProjectException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PomParser property resolution functionality.
 */
class PomParserPropertyTest {
    
    private PomParser parser;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        parser = new PomParser();
    }
    
    @Test
    void testPropertyInterpolation_SimpleProperty_ResolvesCorrectly() throws IOException, MavenProjectException {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>test-project</artifactId>
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
        
        Path pomFile = createTempPom(pomContent);
        Model model = parser.parsePomXml(pomFile.toFile());
        
        assertNotNull(model);
        assertEquals(2, model.getDependencies().size());
        
        // Check that properties were resolved
        Dependency junitDep = findDependency(model, "org.junit.jupiter", "junit-jupiter-api");
        assertNotNull(junitDep);
        assertEquals("5.8.2", junitDep.getVersion());
        
        Dependency mockitoDep = findDependency(model, "org.mockito", "mockito-core");
        assertNotNull(mockitoDep);
        assertEquals("4.6.1", mockitoDep.getVersion());
    }
    
    @Test
    void testPropertyInterpolation_TextProcessorExample_ResolvesCorrectly() throws IOException, MavenProjectException {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.betvictor</groupId>
                <artifactId>text-processor</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                
                <properties>
                    <mockwebserver.version>4.12.0</mockwebserver.version>
                    <awaitility.version>4.2.2</awaitility.version>
                    <testcontainers.version>1.19.3</testcontainers.version>
                </properties>
                
                <dependencies>
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
            </project>
            """;
        
        Path pomFile = createTempPom(pomContent);
        Model model = parser.parsePomXml(pomFile.toFile());
        
        assertNotNull(model);
        assertEquals(2, model.getDependencies().size());
        
        // Check that properties were resolved - this is the exact case that was failing
        Dependency mockwebserverDep = findDependency(model, "com.squareup.okhttp3", "mockwebserver");
        assertNotNull(mockwebserverDep);
        assertEquals("4.12.0", mockwebserverDep.getVersion(), 
            "mockwebserver.version property should be resolved to 4.12.0");
        
        Dependency awaitilityDep = findDependency(model, "org.awaitility", "awaitility");
        assertNotNull(awaitilityDep);
        assertEquals("4.2.2", awaitilityDep.getVersion(), 
            "awaitility.version property should be resolved to 4.2.2");
    }
    
    @Test
    void testPropertyInterpolation_NestedProperties_ResolvesCorrectly() throws IOException, MavenProjectException {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>nested-props-test</artifactId>
                <version>1.0.0</version>
                
                <properties>
                    <spring.version>5.3.21</spring.version>
                    <spring-boot.version>2.7.0</spring-boot.version>
                    <project.version>1.0.0</project.version>
                </properties>
                
                <dependencies>
                    <dependency>
                        <groupId>org.springframework</groupId>
                        <artifactId>spring-core</artifactId>
                        <version>${spring.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter</artifactId>
                        <version>${spring-boot.version}</version>
                    </dependency>
                </dependencies>
            </project>
            """;
        
        Path pomFile = createTempPom(pomContent);
        Model model = parser.parsePomXml(pomFile.toFile());
        
        assertNotNull(model);
        assertEquals(2, model.getDependencies().size());
        
        Dependency springCoreDep = findDependency(model, "org.springframework", "spring-core");
        assertNotNull(springCoreDep);
        assertEquals("5.3.21", springCoreDep.getVersion());
        
        Dependency springBootDep = findDependency(model, "org.springframework.boot", "spring-boot-starter");
        assertNotNull(springBootDep);
        assertEquals("2.7.0", springBootDep.getVersion());
    }
    
    @Test
    void testPropertyInterpolation_UnresolvedProperty_KeepsOriginal() throws IOException, MavenProjectException {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>unresolved-test</artifactId>
                <version>1.0.0</version>
                
                <properties>
                    <known.version>1.0.0</known.version>
                </properties>
                
                <dependencies>
                    <dependency>
                        <groupId>com.example</groupId>
                        <artifactId>known-lib</artifactId>
                        <version>${known.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>com.example</groupId>
                        <artifactId>unknown-lib</artifactId>
                        <version>${unknown.version}</version>
                    </dependency>
                </dependencies>
            </project>
            """;
        
        Path pomFile = createTempPom(pomContent);
        Model model = parser.parsePomXml(pomFile.toFile());
        
        assertNotNull(model);
        assertEquals(2, model.getDependencies().size());
        
        Dependency knownDep = findDependency(model, "com.example", "known-lib");
        assertNotNull(knownDep);
        assertEquals("1.0.0", knownDep.getVersion());
        
        Dependency unknownDep = findDependency(model, "com.example", "unknown-lib");
        assertNotNull(unknownDep);
        assertEquals("${unknown.version}", unknownDep.getVersion(), 
            "Unresolved properties should remain as-is");
    }
    
    @Test
    void testPropertyInterpolation_NoProperties_WorksNormally() throws IOException, MavenProjectException {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>no-props-test</artifactId>
                <version>1.0.0</version>
                
                <dependencies>
                    <dependency>
                        <groupId>org.junit.jupiter</groupId>
                        <artifactId>junit-jupiter-api</artifactId>
                        <version>5.8.2</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """;
        
        Path pomFile = createTempPom(pomContent);
        Model model = parser.parsePomXml(pomFile.toFile());
        
        assertNotNull(model);
        assertEquals(1, model.getDependencies().size());
        
        Dependency junitDep = findDependency(model, "org.junit.jupiter", "junit-jupiter-api");
        assertNotNull(junitDep);
        assertEquals("5.8.2", junitDep.getVersion());
    }
    
    @Test
    void testPropertyInterpolation_DependencyManagement_ResolvesCorrectly() throws IOException, MavenProjectException {
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.test</groupId>
                <artifactId>dep-mgmt-test</artifactId>
                <version>1.0.0</version>
                
                <properties>
                    <testcontainers.version>1.19.3</testcontainers.version>
                </properties>
                
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
                
                <dependencies>
                    <dependency>
                        <groupId>org.testcontainers</groupId>
                        <artifactId>junit-jupiter</artifactId>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """;
        
        Path pomFile = createTempPom(pomContent);
        Model model = parser.parsePomXml(pomFile.toFile());
        
        assertNotNull(model);
        assertNotNull(model.getDependencyManagement());
        assertEquals(1, model.getDependencyManagement().getDependencies().size());
        
        Dependency bomDep = model.getDependencyManagement().getDependencies().get(0);
        assertEquals("org.testcontainers", bomDep.getGroupId());
        assertEquals("testcontainers-bom", bomDep.getArtifactId());
        assertEquals("1.19.3", bomDep.getVersion(), 
            "Property in dependencyManagement should be resolved");
    }
    
    private Path createTempPom(String content) throws IOException {
        Path pomFile = tempDir.resolve("pom.xml");
        Files.writeString(pomFile, content);
        return pomFile;
    }
    
    private Dependency findDependency(Model model, String groupId, String artifactId) {
        return model.getDependencies().stream()
            .filter(dep -> groupId.equals(dep.getGroupId()) && artifactId.equals(dep.getArtifactId()))
            .findFirst()
            .orElse(null);
    }
}