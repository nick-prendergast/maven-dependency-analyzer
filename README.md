# Maven Dependency Analyzer

A command-line tool that identifies Maven projects, analyzes their dependencies (including transitive dependencies), and produces structured JSON output reflecting the dependency hierarchy with SHA1 hashes calculated from the local .m2 Maven repository.

## Features

- ✅ **Accurate Maven Resolution**: Uses official Maven Resolver API for true dependency resolution
- ✅ **Complete Dependency Tree**: Includes all transitive dependencies with proper conflict resolution
- ✅ **SHA1 Hash Calculation**: Computes SHA1 hashes from JARs in local .m2 repository
- ✅ **JSON Output**: Structured, hierarchical dependency tree in JSON format
- ✅ **Cross-Platform**: Works on Windows, macOS, and Linux via Docker
- ✅ **Property Resolution**: Correctly resolves Maven properties like `${version.property}`
- ✅ **Progress Tracking**: Shows real-time progress as dependencies are analyzed
- ✅ **Comprehensive Error Handling**: Graceful handling of missing files and network issues

## Maven Resolution Accuracy

**This tool uses Maven's actual resolution process, not just pom.xml parsing.**

The dependency tree accurately reflects what Maven would use during builds by:

1. **Using Maven Resolver API**: The same engine that powers `mvn dependency:tree`
2. **Conflict Resolution**: Handles version conflicts using Maven's "nearest wins" strategy
3. **Dependency Management**: Respects `<dependencyManagement>` sections for version control
4. **Effective Versions**: Shows the actual versions that would be used in builds
5. **Scope Handling**: Properly processes compile, test, runtime, provided scopes
6. **Transitive Dependencies**: Resolves the complete dependency graph, not just direct dependencies

**Example**: If your pom.xml declares `spring-boot-starter-web:3.0.0` but Maven resolves to `3.0.1` due to transitive dependency conflicts, this tool shows `3.0.1` - the actual version used during builds.

## Requirements

- Docker (for running the analyzer)
- Maven local repository (~/.m2/repository) with resolved dependencies

For development only:
- Java 21 or higher
- Maven 3.6+ (or use included Maven wrapper)


## Getting Started

### Prerequisites
- Docker installed and running
- A Maven project to analyze (with dependencies resolved in your local `~/.m2` repository)

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd maven-dependency-analyzer
   chmod +x analyze
   ```

2. **Build the Docker image:**
   ```bash
   docker build -t maven-dependency-analyzer .
   ```
   
   Note: The `analyze` script will automatically build the image if it doesn't exist.

### Quick Start

```bash
# Analyze current directory (simplest!)
./analyze -d . -o dependencies.json

# Analyze any Maven project
./analyze -d /path/to/maven/project -o output.json

# Get help
./analyze --help
```

### Example Output

When you run the analyzer, you'll see progress tracking:

```
Analyzing project: /path/to/maven/project

Processing dependencies:
  [  1] org.springframework.boot:spring-boot-starter-web:3.5.0 (compile) ✓
  [  2] org.springframework.boot:spring-boot-starter:3.5.0 (compile) ✓
  [  3] org.springframework.boot:spring-boot:3.5.0 (compile) ✓
  ...
  [174] org.testcontainers:kafka:1.21.1 (test) ✓

✓ Analysis completed successfully!
  Total dependencies: 174
  Output file: output.json
```

### Troubleshooting

- **"No dependencies found"**: Run `mvn dependency:resolve` in your test project first
- **"Permission denied"**: Ensure Docker has access to your home directory (`~/.m2`)
- **"pom.xml not found"**: Make sure you're pointing to a valid Maven project directory
- **Custom Maven repository location**: If your `.m2` is not in the default location, update the Docker volume mount in the `analyze` script

## Usage

### Using the Docker Script (Recommended)

The `analyze` script provides the easiest way to run the analyzer:

```bash
# Basic usage
./analyze -d <project-directory> -o <output-file>

# Examples
./analyze -d . -o dependencies.json                    # Current directory
./analyze -d ~/my-project -o analysis.json             # Home directory project
./analyze -d /absolute/path/to/project -o output.json  # Absolute path
```


## Sample Output

```json
{
  "projectPath": "/path/to/maven/project",
  "projectGroupId": "com.example",
  "projectArtifactId": "my-application",
  "projectVersion": "1.0.0",
  "totalDependencies": 15,
  "dependencies": [
    {
      "groupId": "org.springframework.boot",
      "artifactId": "spring-boot-starter-web",
      "version": "3.5.0",
      "sha1": "abc123def456789...",
      "scope": "compile",
      "children": [
        {
          "groupId": "org.springframework.boot",
          "artifactId": "spring-boot-starter",
          "version": "3.5.0",
          "sha1": "def456ghi789...",
          "scope": "compile",
          "children": [
            {
              "groupId": "org.springframework.boot",
              "artifactId": "spring-boot",
              "version": "3.5.0",
              "sha1": "ghi789jkl012...",
              "scope": "compile",
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

## Output Structure

### Root Object
- `projectPath`: Absolute path to the analyzed Maven project
- `projectGroupId`: Project's groupId (inherited from parent if necessary)
- `projectArtifactId`: Project's artifactId
- `projectVersion`: Project's version (inherited from parent if necessary)
- `totalDependencies`: Total count of all dependencies (including transitive)
- `dependencies`: Array of root-level dependencies

### Dependency Object
- `groupId`: Maven groupId of the dependency
- `artifactId`: Maven artifactId of the dependency
- `version`: Resolved version (after conflict resolution)
- `sha1`: SHA1 hash of the JAR file from local .m2 repository (null if not found)
- `scope`: Dependency scope (compile, test, runtime, provided, system, import)
- `children`: Array of transitive dependencies

## Error Handling

### Common Error Scenarios

1. **Invalid Maven Project**
   ```
   Error: No pom.xml found in project directory: /invalid/path
   ```

2. **Missing Required Arguments**
   ```
   Error: Missing required option: -d
   ```

3. **File Not Found**
   ```
   Error: Project path does not exist: /nonexistent/path
   ```

4. **Dependency Resolution Issues**
   ```
   Warning: Failed to calculate SHA1 for file: /path/to/missing.jar
   ```

### Troubleshooting

- **Dependencies not resolved**: Ensure you've run `mvn dependency:resolve` in the target project
- **SHA1 hashes are null**: Dependencies may not be downloaded to local repository
- **Permission errors**: Ensure read access to project directory and .m2 repository
- **Network issues**: Tool works offline with already-downloaded dependencies

## Testing

### Running Unit Tests
```bash
mvn test
```

## Public Libraries Used

As required, all public libraries/tools are documented below:

### Runtime Dependencies
- **Apache Maven Model (3.9.6)** - POM parsing and Maven project model handling
- **Apache Maven Model Builder (3.9.6)** - Maven property interpolation and effective POM building
- **Apache Maven Resolver API (1.9.18)** - Official Maven dependency resolution engine
  - `maven-resolver-api` - Core resolution API
  - `maven-resolver-impl` - Resolution implementation  
  - `maven-resolver-connector-basic` - Repository connectors
  - `maven-resolver-transport-file` - File transport
  - `maven-resolver-transport-http` - HTTP transport
  - `maven-resolver-provider` - Maven integration utilities
- **Apache Commons CLI (1.6.0)** - Command line argument parsing
- **Jackson Databind (2.16.1)** - JSON serialization/deserialization
- **Apache Commons Codec (1.16.0)** - SHA1 hash calculation utilities
- **SLF4J API (2.0.7)** - Logging abstraction
- **Logback Classic (1.4.8)** - Logging implementation

### Test Dependencies
- **JUnit 5 (5.10.1)** - Unit testing framework
- **Mockito (5.8.0)** - Mocking framework for unit tests
