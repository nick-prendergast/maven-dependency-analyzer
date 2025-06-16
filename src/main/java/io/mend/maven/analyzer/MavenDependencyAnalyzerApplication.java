package io.mend.maven.analyzer;

import io.mend.maven.analyzer.cli.CommandLineHandler;
import io.mend.maven.analyzer.config.MavenConstants;
import io.mend.maven.analyzer.config.MavenResolverConfig;
import io.mend.maven.analyzer.exception.DependencyAnalysisException;
import io.mend.maven.analyzer.model.response.AnalysisResult;
import io.mend.maven.analyzer.service.DependencyAnalysisService;
import io.mend.maven.analyzer.service.output.JsonOutputService;
import org.apache.commons.cli.ParseException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MavenDependencyAnalyzerApplication {
    
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
            log.error("Unexpected error", e);
            System.exit(EXIT_CODE_UNEXPECTED_ERROR);
        }
    }
    
    public void run(@NonNull String[] args) throws ParseException, DependencyAnalysisException, IOException {
        CommandLineHandler cliHandler = new CommandLineHandler();
        CommandLineHandler.CommandLineArguments arguments = cliHandler.parseArguments(args);
        
        if (arguments.isHelpRequested()) {
            cliHandler.printHelp();
            return;
        }
        
        String projectPath = arguments.getDirectory();
        String outputPath = arguments.getOutputPath();
        
        String displayPath = getDisplayPath(projectPath);
        
        System.out.println("Analyzing project: " + displayPath);
        System.out.println();
        
        DependencyAnalysisService analysisService = new DependencyAnalysisService(new MavenResolverConfig());
        AnalysisResult analysisResult = analysisService.analyze(projectPath);
        
        JsonOutputService jsonOutputService = new JsonOutputService();
        jsonOutputService.writeToFile(analysisResult, outputPath);
        
        printSuccessSummary(analysisResult.getTotalDependencies(), outputPath);
    }
    
    private String getDisplayPath(String projectPath) {
        String dockerPath = System.getenv(MavenConstants.ENV_ORIGINAL_PROJECT_PATH);
        return (dockerPath != null && !dockerPath.isEmpty()) ? dockerPath : projectPath;
    }
    
    private void printSuccessSummary(int totalDependencies, String outputPath) {
        System.out.println();
        System.out.println("✓ Analysis completed successfully!");
        System.out.println("  Total dependencies: " + totalDependencies);
        System.out.println("  Output file: " + outputPath);
    }
}
