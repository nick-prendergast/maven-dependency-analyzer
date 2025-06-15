package io.mend.maven.analyzer.cli;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineHandlerTest {
    
    private CommandLineHandler handler;
    
    @BeforeEach
    void setUp() {
        handler = new CommandLineHandler();
    }
    
    @Test
    void testParseArguments_ValidArgs_ReturnsCorrectArguments() throws ParseException {
        String[] args = {"-d", "/path/to/project", "-o", "output.json"};
        
        CommandLineHandler.CommandLineArguments result = handler.parseArguments(args);
        
        assertFalse(result.isHelpRequested());
        assertEquals("/path/to/project", result.getDirectory());
        assertEquals("output.json", result.getOutputPath());
    }
    
    @Test
    void testParseArguments_LongFlags_ReturnsCorrectArguments() throws ParseException {
        String[] args = {"--directory", "/path/to/project", "--output", "output.json"};
        
        CommandLineHandler.CommandLineArguments result = handler.parseArguments(args);
        
        assertFalse(result.isHelpRequested());
        assertEquals("/path/to/project", result.getDirectory());
        assertEquals("output.json", result.getOutputPath());
    }
    
    @Test
    void testParseArguments_HelpFlag_ReturnsHelpRequested() throws ParseException {
        String[] args = {"--help"};
        
        CommandLineHandler.CommandLineArguments result = handler.parseArguments(args);
        
        assertTrue(result.isHelpRequested());
        assertNull(result.getDirectory());
        assertNull(result.getOutputPath());
    }
    
    @Test
    void testParseArguments_MissingDirectory_ThrowsParseException() {
        String[] args = {"-o", "output.json"};
        
        ParseException exception = assertThrows(ParseException.class, 
            () -> handler.parseArguments(args));
        
        assertTrue(exception.getMessage().contains("Missing required option: -d"));
    }
    
    @Test
    void testParseArguments_MissingOutput_ThrowsParseException() {
        String[] args = {"-d", "/path/to/project"};
        
        ParseException exception = assertThrows(ParseException.class, 
            () -> handler.parseArguments(args));
        
        assertTrue(exception.getMessage().contains("Missing required option: -o"));
    }
    
    @Test
    void testParseArguments_EmptyArgs_ThrowsParseException() {
        String[] args = {};
        
        assertThrows(ParseException.class, () -> handler.parseArguments(args));
    }
    
    @Test
    void testParseArguments_InvalidFlag_ThrowsParseException() {
        String[] args = {"-x", "invalid"};
        
        assertThrows(ParseException.class, () -> handler.parseArguments(args));
    }
    
    @Test
    void testPrintHelp_DoesNotThrow() {
        assertDoesNotThrow(() -> handler.printHelp());
    }
}