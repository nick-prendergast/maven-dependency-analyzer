package io.mend.maven.analyzer.cli;

import org.apache.commons.cli.*;

public class CommandLineHandler {
    
    private static final String OPTION_DIRECTORY = "d";
    private static final String OPTION_OUTPUT = "o";
    private static final String OPTION_HELP = "help";
    
    private final Options options;
    
    public CommandLineHandler() {
        this.options = createOptions();
    }
    
    private Options createOptions() {
        Options options = new Options();
        
        Option directoryOption = Option.builder(OPTION_DIRECTORY)
                .longOpt("directory")
                .hasArg()
                .argName("path")
                .desc("Directory to be scanned (Maven project root)")
                .required(false)
                .build();
        
        Option outputOption = Option.builder(OPTION_OUTPUT)
                .longOpt("output")
                .hasArg()
                .argName("file")
                .desc("Output path for the resulting JSON file")
                .required(false)
                .build();
        
        Option helpOption = Option.builder()
                .longOpt(OPTION_HELP)
                .desc("Display usage information and describe available commands/flags")
                .build();
        
        options.addOption(directoryOption);
        options.addOption(outputOption);
        options.addOption(helpOption);
        
        return options;
    }
    
    public CommandLineArguments parseArguments(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        
        if (cmd.hasOption(OPTION_HELP)) {
            return new CommandLineArguments(true, null, null);
        }
        
        String directory = cmd.getOptionValue(OPTION_DIRECTORY);
        String output = cmd.getOptionValue(OPTION_OUTPUT);
        
        if (directory == null) {
            throw new ParseException("Missing required option: -" + OPTION_DIRECTORY);
        }
        
        if (output == null) {
            throw new ParseException("Missing required option: -" + OPTION_OUTPUT);
        }
        
        return new CommandLineArguments(false, directory, output);
    }
    
    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("maven-dependency-analyzer", 
                "CLI tool to analyze Maven project dependencies and generate JSON hierarchy", 
                options, 
                "\nExamples:\n" +
                "  maven-dependency-analyzer -d /path/to/maven/project -o dependencies.json\n" +
                "  maven-dependency-analyzer --directory /path/to/project --output output.json\n" +
                "  maven-dependency-analyzer --help\n\n" +
                "This tool identifies Maven projects, analyzes their dependencies (including transitive dependencies),\n" +
                "and produces a structured JSON output reflecting the dependency hierarchy with SHA1 hashes\n" +
                "calculated from the local .m2 Maven repository.",
                true);
    }
    
    public static class CommandLineArguments {
        private final boolean helpRequested;
        private final String directory;
        private final String outputPath;
        
        public CommandLineArguments(boolean helpRequested, String directory, String outputPath) {
            this.helpRequested = helpRequested;
            this.directory = directory;
            this.outputPath = outputPath;
        }
        
        public boolean isHelpRequested() {
            return helpRequested;
        }
        
        public String getDirectory() {
            return directory;
        }
        
        public String getOutputPath() {
            return outputPath;
        }
    }
}