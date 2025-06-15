# Maven Dependency Analyzer - Code Guide

## What This Project Does

This is a take-home assignment demonstrating a CLI tool that:

1. **Analyzes Maven Projects**: Scans a Maven project directory to find and resolve all dependencies
2. **Resolves Transitive Dependencies**: Uses Maven's actual resolution logic (not just reading pom.xml)
3. **Calculates SHA1 Hashes**: Computes SHA1 hashes from JAR files in your local .m2 repository
4. **Generates JSON Output**: Creates a structured JSON file with the complete dependency tree

**Key Requirements Met:**
- CLI with `-d`, `-o`, `--help` flags
- Maven project detection
- Accurate dependency resolution using Maven Resolver API
- SHA1 hash calculation from local repository
- JSON output with hierarchical structure

## How It Works

```bash
# Basic usage
mvn exec:java -Dexec.args="-d /path/to/maven/project -o dependencies.json"

# Example output structure
{
  "projectPath": "/path/to/project",
  "projectGroupId": "com.example",
  "projectArtifactId": "my-app", 
  "projectVersion": "1.0.0",
  "dependencies": [
    {
      "groupId": "org.junit.jupiter",
      "artifactId": "junit-jupiter-api",
      "version": "5.8.2",
      "sha1": "abc123...",
      "scope": "test",
      "children": [ /* nested dependencies */ ]
    }
  ],
  "totalDependencies": 15
}
```

## Table of Contents

1. [Project Structure](#project-structure)
2. [How It's Built](#how-its-built)
3. [Key Components](#key-components)
4. [Running the Project](#running-the-project)
5. [Testing](#testing)
6. [Understanding the Code Flow](#understanding-the-code-flow)

## Project Structure

```
src/main/java/io/mend/maven/analyzer/
├── MavenDependencyAnalyzerApplication.java   # Main entry point
├── cli/
│   └── CommandLineHandler.java               # Handles -d, -o, --help flags
├── config/
│   └── MavenResolverConfig.java              # Sets up Maven resolution
├── exception/
│   ├── DependencyAnalysisException.java      # Custom exceptions
│   ├── MavenProjectException.java            
│   └── GlobalExceptionHandler.java           
├── model/
│   ├── entity/
│   │   └── AnalyzedDependency.java           # Represents a dependency in our model
│   └── response/
│       └── AnalysisResult.java               # Final JSON output structure
├── service/
│   ├── DependencyAnalysisService.java        # Main orchestration service
│   ├── detection/
│   │   ├── MavenProjectDetectionService.java # Orchestrates Maven project detection
│   │   ├── PomValidator.java                 # Validates project structure and POM files
│   │   ├── PomParser.java                    # Parses POM files and creates Maven models
│   │   └── ModelPropertyExtractor.java       # Extracts effective properties from models
│   ├── analysis/
│   │   ├── DependencyResolverService.java    # Resolves dependencies using Maven API
│   │   └── DependencyTreeBuilderService.java # Builds dependency tree structure
│   ├── hash/
│   │   └── Sha1HashService.java              # Calculates SHA1 hashes from local repo
│   └── output/
│       └── JsonOutputService.java            # Writes JSON output files
└── util/
    ├── SecurityValidator.java                # Input validation and security
    └── DependencyUtils.java                  # Utility methods for dependency operations
└── util/
    └── SecurityValidator.java                # Input validation

src/main/resources/
└── logback-spring.xml                        # Logging configuration

src/test/java/
├── 67 test classes covering all functionality
└── integration/
    └── FullAnalysisIntegrationTest.java      # End-to-end tests
```

## How It's Built

### 1. **Maven Dependencies**

The project uses these key libraries:

```xml
<!-- Maven Resolution (the core functionality) -->
<dependency>
    <groupId>org.apache.maven.resolver</groupId>
    <artifactId>maven-resolver-api</artifactId>
</dependency>

<!-- Command Line Parsing -->
<dependency>
    <groupId>commons-cli</groupId>
    <artifactId>commons-cli</artifactId>
</dependency>

<!-- JSON Output -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Logging -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</dependency>
```

### 2. **Build Process**

```bash
# Compile the code
mvn compile

# Run all tests
mvn test

# Create executable JAR
mvn package
```

## Key Components

### 1. **Main Application (`MavenDependencyAnalyzerApplication.java`)**

This is the entry point that:
- Parses command line arguments using `CommandLineHandler`
- Delegates the analysis to `DependencyAnalysisService`
- Writes the result to a JSON file

```java
public void run(String[] args) throws ParseException, DependencyAnalysisException, IOException {
    CommandLineHandler cliHandler = new CommandLineHandler();
    CommandLineHandler.CommandLineArguments arguments = cliHandler.parseArguments(args);
    
    if (arguments.isHelpRequested()) {
        cliHandler.printHelp();
        return;
    }
    
    String projectPath = arguments.getDirectory();
    String outputPath = arguments.getOutputPath();
    
    // Analyze the project
    DependencyAnalysisService analysisService = new DependencyAnalysisService(new MavenResolverConfig());
    AnalysisResult result = analysisService.analyze(projectPath);
    
    // Write JSON output
    JsonOutputService jsonOutputService = new JsonOutputService();
    jsonOutputService.writeToFile(result, outputPath);
    
    System.out.println("Analysis completed successfully!");
    System.out.println("Total dependencies found: " + result.getTotalDependencies());
}
```

### 2. **Dependency Analysis Service (`DependencyAnalysisService.java`)**

This orchestrates the entire analysis process:

```java
public AnalysisResult analyze(String projectPath) throws DependencyAnalysisException {
    // 1. Validate the project path
    Path normalizedPath = SecurityValidator.validateAndNormalizePath(projectPath);
    
    // 2. Check it's a valid Maven project
    detectionService.validateMavenProject(safePath);
    Model projectModel = detectionService.readPomModel(safePath);
    
    // 3. Resolve all dependencies using Maven's logic
    org.eclipse.aether.graph.DependencyNode rootNode = resolverService.resolveDependencies(projectModel);
    
    // 4. Build the tree with SHA1 hashes
    List<AnalyzedDependency> dependencies = treeBuilderService.buildDependencyTree(rootNode);
    
    // 5. Create the final result
    AnalysisResult result = new AnalysisResult(safePath, projectGroupId, projectArtifactId, projectVersion);
    result.setDependencies(dependencies);
    
    return result;
}
```

### 3. **Core Services**

**Detection Services**:

**`MavenProjectDetectionService`**: 
- Orchestrates Maven project detection and validation
- Coordinates with specialized validation, parsing, and extraction services
- Provides high-level interface for project analysis

**`PomValidator`**: 
- Validates project directory structure
- Ensures pom.xml exists and is accessible
- Performs basic file system validations

**`PomParser`**: 
- Parses pom.xml files using Maven's XML parser
- Validates Maven model structure
- Ensures required fields (groupId, artifactId) are present

**`ModelPropertyExtractor`**: 
- Extracts effective properties from Maven models
- Handles inheritance from parent POMs
- Resolves effective groupId and version values

**`DependencyResolverService`**: 
- Uses Maven Resolver API to resolve dependencies
- Handles transitive dependencies and version conflicts
- Returns Maven's actual dependency resolution (not just what's in pom.xml)

**`DependencyTreeBuilderService`**: 
- Converts Maven's dependency graph into our `AnalyzedDependency` tree structure
- Calls SHA1 service to calculate hashes for each dependency
- Handles duplicate dependency detection and removal
- Processes child dependencies recursively with reduced nesting complexity

**`Sha1HashService`**: 
- Finds JAR files in your local ~/.m2/repository
- Calculates SHA1 hash of each JAR file using Apache Commons Codec
- Uses proper logging instead of System.err for error reporting
- Validates file existence before attempting hash calculation

**`JsonOutputService`**: 
- Converts the result to JSON format
- Writes it to the specified output file

### 4. **Data Models**

**`AnalyzedDependency`**: Represents a single dependency in our model
```java
public class AnalyzedDependency {
    private String groupId;      // e.g., "org.junit.jupiter"  
    private String artifactId;   // e.g., "junit-jupiter-api"
    private String version;      // e.g., "5.8.2"
    private String sha1;         // e.g., "abc123..."
    private String scope;        // e.g., "test", "compile"
    private List<AnalyzedDependency> children; // nested dependencies
}
```

**Note**: This class was renamed from `DependencyNode` to avoid naming conflicts with Maven's `org.eclipse.aether.graph.DependencyNode` API class.

**`AnalysisResult`**: The final output structure
```java
public class AnalysisResult {
    private String projectPath;
    private String projectGroupId;
    private String projectArtifactId;
    private String projectVersion;
    private List<AnalyzedDependency> dependencies;
    private int totalDependencies;
}
```

## Running the Project

### 1. **Quick Start (Docker - Recommended)**

```bash
# Make script executable
chmod +x analyze.sh

# Analyze any Maven project
./analyze.sh /path/to/maven/project output.json

# Analyze current directory
./analyze.sh . my-dependencies.json
```

### 2. **Docker Commands**

```bash
# Build Docker image
docker build -t maven-dependency-analyzer .

# Run with Docker
docker run --rm \
  -v ~/.m2:/root/.m2 \
  -v /path/to/maven/project:/project:ro \
  -v $(pwd)/output:/output \
  maven-dependency-analyzer

# Using docker-compose
PROJECT_PATH=/path/to/project docker-compose up maven-analyzer
```

### 3. **Direct Maven Commands**

```bash
# Compile everything
mvn compile

# Run all tests (67 tests)
mvn test

# Create the JAR file
mvn package

# Clean and rebuild everything
mvn clean package
```

### 4. **Running the Application Directly**

```bash
# Show help
mvn exec:java -Dexec.args="--help"

# Analyze a Maven project
mvn exec:java -Dexec.args="-d /path/to/maven/project -o dependencies.json"

# Example with a real project
mvn exec:java -Dexec.args="-d ../some-maven-project -o output.json"
```

### 5. **Running from JAR**

```bash
# After mvn package
java -jar target/maven-dependency-analyzer-0.0.1-SNAPSHOT-shaded.jar -d /path/to/project -o output.json
```

## Testing

### 1. **Test Structure**

The project has **67 tests** covering:

- **Unit Tests**: Each service class is tested individually
- **Integration Tests**: End-to-end testing of the CLI
- **Error Cases**: Testing invalid inputs, missing files, etc.

### 2. **Key Test Files**

**`FullAnalysisIntegrationTest.java`** (the file you have open):
- Creates temporary Maven projects for testing
- Runs the full CLI workflow
- Verifies JSON output and console messages
- Tests error scenarios (invalid projects, missing files)

**Example test:**
```java
@Test
void testFullAnalysis_ValidProject_GeneratesJsonOutput() throws Exception {
    // Create a fake Maven project with pom.xml
    Path projectDir = tempDir.resolve("test-project");
    Files.createDirectories(projectDir);
    Files.writeString(projectDir.resolve("pom.xml"), TestConstants.SAMPLE_POM_XML);
    
    // Run the analyzer
    String[] args = {"-d", projectDir.toString(), "-o", outputFile.toString()};
    application.run(args);
    
    // Check the output file was created with correct content
    assertTrue(Files.exists(outputFile));
    String jsonContent = Files.readString(outputFile);
    assertTrue(jsonContent.contains("\"projectGroupId\""));
    assertTrue(jsonContent.contains("\"dependencies\""));
}
```

### 3. **Running Tests**

```bash
# Run all tests
mvn test

# Run just integration tests
mvn test -Dtest=*IntegrationTest

# Run a specific test
mvn test -Dtest=FullAnalysisIntegrationTest
```

## Understanding the Code Flow

### **Step-by-Step Execution:**

1. **CLI Parsing** (`CommandLineHandler`)
   - Parses `-d`, `-o`, `--help` flags
   - Validates required arguments are present

2. **Project Validation** (`MavenProjectDetectionService`)
   - Checks if directory exists
   - Verifies pom.xml is present and valid
   - Extracts project information (groupId, artifactId, version)

3. **Dependency Resolution** (`DependencyResolverService`)
   - Uses Maven Resolver API (same as `mvn dependency:tree`)
   - Resolves transitive dependencies
   - Handles version conflicts using Maven's rules

4. **Tree Building** (`DependencyTreeBuilderService`)
   - Converts Maven's dependency graph to our tree structure
   - Calculates SHA1 hashes for each JAR file

5. **JSON Output** (`JsonOutputService`)
   - Serializes the result to JSON
   - Writes to the specified output file

### **Key Files to Understand:**

- **`MavenDependencyAnalyzerApplication.java`**: Start here - main entry point
- **`DependencyAnalysisService.java`**: The orchestrator that coordinates everything
- **`AnalysisResult.java`**: The final output structure (what becomes JSON)
- **`DependencyNode.java`**: Represents each dependency in the tree

### **Maven Dependencies Used:**

- **Maven Resolver API**: For accurate dependency resolution
- **Apache Commons CLI**: For parsing command line arguments  
- **Jackson**: For JSON serialization
- **SLF4J + Logback**: For logging
- **JUnit 5 + Mockito**: For testing

This project demonstrates a clean, well-tested CLI application that solves a real problem using Maven's official APIs.