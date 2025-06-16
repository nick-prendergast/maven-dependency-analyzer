package io.mend.maven.analyzer.config;

import lombok.experimental.UtilityClass;

/**
 * Constants used throughout the Maven Dependency Analyzer application.
 */
@UtilityClass
public class MavenConstants {
    
    // Repository constants
    public static final String DEFAULT_MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";
    public static final String DEFAULT_MAVEN_CENTRAL_ID = "central";
    public static final String DEFAULT_REPOSITORY_TYPE = "default";
    public static final String DEFAULT_M2_REPOSITORY_PATH = "/.m2/repository";
    
    // File names and extensions
    public static final String JAR_EXTENSION = ".jar";
    public static final String POM_XML_FILENAME = "pom.xml";
    public static final String ARTIFACT_FILENAME_SEPARATOR = "-";
    
    // System properties
    public static final String USER_HOME_PROPERTY = "user.home";
    public static final String MAVEN_LOCAL_REPO_PROPERTY = "maven.repo.local";
    
    // Environment variables
    public static final String ENV_ORIGINAL_PROJECT_PATH = "ORIGINAL_PROJECT_PATH";
    
    // Property interpolation
    public static final String PROPERTY_PLACEHOLDER_PREFIX = "${";
    public static final String PROPERTY_PLACEHOLDER_REGEX = "\\$\\{([^}]+)\\}";

    // Coordinate formats
    public static final String COORDINATE_FORMAT = "%s:%s:%s:%s";
}