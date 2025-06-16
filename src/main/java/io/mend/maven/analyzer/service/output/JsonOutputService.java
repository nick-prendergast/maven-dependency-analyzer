package io.mend.maven.analyzer.service.output;

import io.mend.maven.analyzer.model.response.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonOutputService {
    
    private final ObjectMapper objectMapper;
    
    public JsonOutputService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    public void writeToFile(@NonNull AnalysisResult result, @NonNull String outputPath) throws IOException {
        if (outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be empty");
        }
        
        Path path = Paths.get(outputPath);
        
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        File outputFile = path.toFile();
        objectMapper.writeValue(outputFile, result);
    }
    
}