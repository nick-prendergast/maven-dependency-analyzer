package io.mend.maven.analyzer;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestConstants {
    
    public static final String TEST_PROJECT_GROUP_ID = "com.test";
    public static final String TEST_PROJECT_ARTIFACT_ID = "test-project";
    public static final String TEST_PROJECT_VERSION = "1.0.0";
    
    public static final String TEST_DEPENDENCY_GROUP_ID = "org.junit.jupiter";
    public static final String TEST_DEPENDENCY_ARTIFACT_ID = "junit-jupiter-api";
    public static final String TEST_DEPENDENCY_VERSION = "5.8.2";
    public static final String TEST_DEPENDENCY_SCOPE = "test";
    
    public static final String SAMPLE_SHA1_HASH = "abc123def456ghi789jkl012mno345pqr678stu901";
    
    public static final String SAMPLE_POM_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                
                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>%s</version>
                
                <properties>
                    <maven.compiler.source>21</maven.compiler.source>
                    <maven.compiler.target>21</maven.compiler.target>
                </properties>
                
                <dependencies>
                    <dependency>
                        <groupId>%s</groupId>
                        <artifactId>%s</artifactId>
                        <version>%s</version>
                        <scope>%s</scope>
                    </dependency>
                </dependencies>
            </project>
            """.formatted(
                TEST_PROJECT_GROUP_ID, TEST_PROJECT_ARTIFACT_ID, TEST_PROJECT_VERSION,
                TEST_DEPENDENCY_GROUP_ID, TEST_DEPENDENCY_ARTIFACT_ID, TEST_DEPENDENCY_VERSION, TEST_DEPENDENCY_SCOPE
            );
    
    public static final String EMPTY_POM_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                
                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>%s</version>
                
                <properties>
                    <maven.compiler.source>21</maven.compiler.source>
                    <maven.compiler.target>21</maven.compiler.target>
                </properties>
            </project>
            """.formatted(TEST_PROJECT_GROUP_ID, TEST_PROJECT_ARTIFACT_ID, TEST_PROJECT_VERSION);
    
    public static final String INVALID_POM_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <modelVersion>4.0.0</modelVersion>
                <!-- Missing required elements -->
            </project>
            """;
    
}