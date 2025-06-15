package io.mend.maven.analyzer.exception;

public class MavenProjectException extends Exception {
    
    public MavenProjectException(String message) {
        super(message);
    }
    
    public MavenProjectException(String message, Throwable cause) {
        super(message, cause);
    }
}