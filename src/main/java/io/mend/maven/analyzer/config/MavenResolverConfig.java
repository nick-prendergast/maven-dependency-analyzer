package io.mend.maven.analyzer.config;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MavenResolverConfig {
    
    private static final String DEFAULT_LOCAL_REPO_PATH = MavenConstants.DEFAULT_M2_REPOSITORY_PATH;
    private static final char GROUP_ID_SEPARATOR = '.';
    private static final char PATH_SEPARATOR = '/';
    
    private static final String USER_HOME = System.getProperty(MavenConstants.USER_HOME_PROPERTY);
    private static final String DEFAULT_LOCAL_REPO = USER_HOME + DEFAULT_LOCAL_REPO_PATH;
    
    @Getter
    private final RepositorySystem repositorySystem;
    @Getter
    private final RepositorySystemSession session;
    @Getter
    private final List<RemoteRepository> repositories;
    
    public MavenResolverConfig() {
        this.repositorySystem = createRepositorySystem();
        this.session = createRepositorySystemSession(repositorySystem);
        this.repositories = createRemoteRepositories();
    }
    
    private RepositorySystem createRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                log.error("Service creation failed for {} with implementation {}", type.getName(), impl.getName(), exception);
            }
        });
        
        return locator.getService(RepositorySystem.class);
    }
    
    private RepositorySystemSession createRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        
        String localRepoPath = System.getProperty(MavenConstants.MAVEN_LOCAL_REPO_PROPERTY, DEFAULT_LOCAL_REPO);
        LocalRepository localRepo = new LocalRepository(localRepoPath);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        
        return session;
    }
    
    private List<RemoteRepository> createRemoteRepositories() {
        List<RemoteRepository> repositories = new ArrayList<>();
        
        repositories.add(new RemoteRepository.Builder(
            MavenConstants.DEFAULT_MAVEN_CENTRAL_ID, 
            MavenConstants.DEFAULT_REPOSITORY_TYPE, 
            MavenConstants.DEFAULT_MAVEN_CENTRAL_URL).build());
        
        return repositories;
    }
    
    public String getLocalRepositoryPath() {
        return session.getLocalRepository().getBasedir().getAbsolutePath();
    }
    
    public File getLocalRepositoryFile(String groupId, String artifactId, String version) {
        String path = groupId.replace(GROUP_ID_SEPARATOR, PATH_SEPARATOR) + PATH_SEPARATOR + 
                     artifactId + PATH_SEPARATOR + version + PATH_SEPARATOR + 
                     artifactId + MavenConstants.ARTIFACT_FILENAME_SEPARATOR + version + MavenConstants.JAR_EXTENSION;
        return Paths.get(getLocalRepositoryPath(), path).toFile();
    }
}