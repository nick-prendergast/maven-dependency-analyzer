package io.mend.maven.analyzer.service.hash;

import io.mend.maven.analyzer.config.MavenResolverConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Sha1HashServiceTest {
    
    @Mock
    private MavenResolverConfig config;
    
    private Sha1HashService service;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new Sha1HashService(config);
    }
    
    @Test
    void testCalculateSha1Hash_ValidFile_ReturnsHash() throws IOException {
        // Create a test file with known content
        Path testFile = tempDir.resolve("test.jar");
        String testContent = "Hello, World!";
        Files.writeString(testFile, testContent);
        
        String hash = service.calculateSha1Hash(testFile.toFile());
        
        assertNotNull(hash);
        assertEquals(40, hash.length()); // SHA1 is always 40 characters
        assertTrue(hash.matches("[a-f0-9]+"));  // SHA1 contains only hex characters
    }
    
    @Test
    void testCalculateSha1Hash_NonExistentFile_ReturnsNull() {
        File nonExistentFile = new File(tempDir.toFile(), "non-existent.jar");
        
        String hash = service.calculateSha1Hash(nonExistentFile);
        
        assertNull(hash);
    }
    
    @Test
    void testCalculateSha1Hash_NullFile_ReturnsNull() {
        String hash = service.calculateSha1Hash((File) null);
        
        assertNull(hash);
    }
    
    @Test
    void testCalculateSha1Hash_Directory_ReturnsNull() throws IOException {
        // Create a directory instead of file
        Path testDir = tempDir.resolve("test-directory");
        Files.createDirectories(testDir);
        
        String hash = service.calculateSha1Hash(testDir.toFile());
        
        assertNull(hash);
    }
    
    @Test
    void testCalculateSha1Hash_WithCoordinates_UsesConfig() throws IOException {
        String groupId = "com.example";
        String artifactId = "test-artifact";
        String version = "1.0.0";
        
        // Create a test file
        Path testFile = tempDir.resolve("test.jar");
        String testContent = "Test JAR content";
        Files.writeString(testFile, testContent);
        
        // Mock the config to return our test file
        when(config.getLocalRepositoryFile(groupId, artifactId, version))
            .thenReturn(testFile.toFile());
        
        String hash = service.calculateSha1Hash(groupId, artifactId, version);
        
        assertNotNull(hash);
        verify(config).getLocalRepositoryFile(groupId, artifactId, version);
    }
    
    @Test
    void testCalculateSha1Hash_WithCoordinates_FileNotExists_ReturnsNull() {
        String groupId = "com.example";
        String artifactId = "test-artifact";
        String version = "1.0.0";
        
        File nonExistentFile = new File(tempDir.toFile(), "non-existent.jar");
        when(config.getLocalRepositoryFile(groupId, artifactId, version))
            .thenReturn(nonExistentFile);
        
        String hash = service.calculateSha1Hash(groupId, artifactId, version);
        
        assertNull(hash);
        verify(config).getLocalRepositoryFile(groupId, artifactId, version);
    }
    
}