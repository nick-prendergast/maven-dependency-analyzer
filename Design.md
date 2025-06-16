# Design Document - Maven Dependency Analyzer

## Overview

This document outlines the design and implementation of a CLI tool that analyzes Maven project dependencies and outputs them in a hierarchical JSON format. The tool provides accurate dependency resolution, SHA1 hash calculation, and handles complex Maven features like property interpolation and dependency management.

## Approach to Maven Parsing

### Core Approach
I chose to leverage **Maven's official Resolver API** rather than parsing pom.xml files manually. This decision ensures the tool behaves exactly like Maven itself when resolving dependencies.

**Key Components:**
- `PomParser`: Reads and validates pom.xml files using Maven's ModelBuilder
- `ModelPropertyExtractor`: Handles ${property} interpolation when ModelBuilder isn't sufficient
- Fallback mechanism for cases where parent POMs are missing

## Strategy for Dependency Tree Construction

### Two-Phase Approach

**Phase 1: Resolution**
- Use Maven Resolver to get the complete dependency graph
- Handle version conflicts automatically (Maven's "nearest wins" rule)

**Phase 2: Tree Building**
- Convert Maven's graph to our JSON structure
- Calculate SHA1 hashes for each JAR file
- Track visited dependencies to prevent duplicates

**Key Features:**
- Progress reporting during analysis
- Graceful handling of missing JARs (returns null SHA1)
- Deduplication using `groupId:artifactId:version:scope` as unique key

## Architecture Choices for CLI Input/Output Handling

### Clean Architecture Design

```
CLI Layer → Service Layer → Domain Services → External Libraries
```

**CLI Layer**: Handles command-line arguments and user interaction
- Uses Apache Commons CLI for parsing `-d` and `-o` options
- Validates inputs and provides clear error messages

**Service Layer**: Orchestrates the analysis workflow
- `DependencyAnalysisService` acts as the main facade
- Coordinates parsing, resolution, and output generation

**Output Handling**:
- Jackson for JSON serialization
- Pretty-printed, hierarchical format
- Progress reporting to stderr (keeps stdout clean for piping)

## Key Design Decisions

### Why Use Maven Resolver API?
Using Maven's official API ensures the tool behaves exactly like Maven itself. Manual parsing would miss important features like:
- Version conflict resolution
- Dependency management inheritance
- Property interpolation
- Transitive dependency handling

### Error Handling Philosophy
- **Fail fast** for invalid inputs
- **Graceful degradation** for missing JARs (continue analysis, SHA1 = null)
- **Clear error messages** with specific exit codes

### Security & Performance
- Path validation prevents directory traversal attacks
- Deduplication avoids analyzing dependencies multiple times
- Progress output helps users track long-running analyses

## Example Output

For a project with Spring Boot dependencies:
```json
{
  "projectPath": "/path/to/project",
  "projectGroupId": "com.example",
  "projectArtifactId": "my-app",
  "projectVersion": "1.0.0",
  "dependencies": [
    {
      "groupId": "org.springframework.boot",
      "artifactId": "spring-boot-starter-web",
      "version": "3.2.0",
      "scope": "compile",
      "sha1": "abc123...",
      "dependencies": [...]
    }
  ],
  "totalDependencies": 42
}
```

## Summary

This design delivers a robust CLI tool that accurately analyzes Maven dependencies by leveraging official Maven APIs, following clean architecture principles, and providing clear, actionable output. The modular structure makes it easy to extend with new features while maintaining the core functionality.