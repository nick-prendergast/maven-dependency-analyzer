package io.mend.maven.analyzer.service.hash;

import io.mend.maven.analyzer.config.MavenResolverConfig;
import org.apache.commons.codec.digest.DigestUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class Sha1HashService {
    
    private final MavenResolverConfig config;
    
    public Sha1HashService(@NonNull MavenResolverConfig config) {
        this.config = config;
    }
    
    public String calculateSha1Hash(@NonNull String groupId, @NonNull String artifactId, @NonNull String version) {
        File jarFile = getJarFile(groupId, artifactId, version);
        return calculateSha1Hash(jarFile);
    }
    
    public String calculateSha1Hash(File file) {
        if (!isValidJarFile(file)) {
            return null;
        }
        
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.sha1Hex(fileInputStream);
        } catch (IOException e) {
            log.warn("Failed to calculate SHA1 for file: {} - {}", file.getAbsolutePath(), e.getMessage());
            return null;
        }
    }
    
    private boolean isValidJarFile(File file) {
        return file != null && file.exists() && file.isFile();
    }
    
    private File getJarFile(String groupId, String artifactId, String version) {
        return config.getLocalRepositoryFile(groupId, artifactId, version);
    }
}