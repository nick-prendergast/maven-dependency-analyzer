package io.mend.maven.analyzer;

import io.mend.maven.analyzer.cli.CommandLineHandler;
import io.mend.maven.analyzer.config.MavenResolverConfig;
import io.mend.maven.analyzer.exception.DependencyAnalysisException;
import io.mend.maven.analyzer.model.response.AnalysisResult;
import io.mend.maven.analyzer.service.DependencyAnalysisService;
import io.mend.maven.analyzer.service.output.JsonOutputService;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main application class for the Maven Dependency Analyzer CLI tool.
 */
public class MavenDependencyAnalyzerApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(MavenDependencyAnalyzerApplication.class);
    
    // Exit codes for different error conditions
    private static final int EXIT_CODE_PARSE_ERROR = 1;
    private static final int EXIT_CODE_ANALYSIS_ERROR = 2;
    private static final int EXIT_CODE_IO_ERROR = 3;
    private static final int EXIT_CODE_UNEXPECTED_ERROR = 4;

    public static void main(String[] args) {
        try {
            new MavenDependencyAnalyzerApplication().run(args);
        } catch (ParseException e) {
            System.err.println("Invalid command line arguments: " + e.getMessage());
            System.exit(EXIT_CODE_PARSE_ERROR);
        } catch (DependencyAnalysisException e) {
            System.err.println("Analysis error: " + e.getMessage());
            System.exit(EXIT_CODE_ANALYSIS_ERROR);
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            System.exit(EXIT_CODE_IO_ERROR);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            logger.error("Unexpected error", e);
            System.exit(EXIT_CODE_UNEXPECTED_ERROR);
        }
    }
    
    public void run(String[] args) throws ParseException, DependencyAnalysisException, IOException {
        CommandLineHandler cliHandler = new CommandLineHandler();
        CommandLineHandler.CommandLineArguments arguments = cliHandler.parseArguments(args);
        
        if (arguments.isHelpRequested()) {
            cliHandler.printHelp();
            return;
        }
        
        String projectPath = arguments.getDirectory();
        String outputPath = arguments.getOutputPath();
        
        // Get the original path from environment variable if running in Docker
        String displayPath = projectPath;
        String originalPath = System.getenv("ORIGINAL_PROJECT_PATH");
        if (originalPath != null && !originalPath.isEmpty()) {
            displayPath = originalPath;
        }
        
        System.out.println("Analyzing project: " + displayPath);
        System.out.println();
        
        // Use the service facade for analysis
        DependencyAnalysisService analysisService = new DependencyAnalysisService(new MavenResolverConfig());
        AnalysisResult result = analysisService.analyze(projectPath);
        
        // Write output
        JsonOutputService jsonOutputService = new JsonOutputService();
        jsonOutputService.writeToFile(result, outputPath);
        
        System.out.println();
        System.out.println("✓ Analysis completed successfully!");
        System.out.println("  Total dependencies: " + result.getTotalDependencies());
        System.out.println("  Output file: " + outputPath);
    }
}
