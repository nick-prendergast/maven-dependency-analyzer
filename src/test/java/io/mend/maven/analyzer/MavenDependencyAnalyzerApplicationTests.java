package io.mend.maven.analyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MavenDependencyAnalyzerApplicationTests {

	@Test
	void testMainMethodExists() {
		// Simple test to verify the main method exists and class loads
		assertNotNull(MavenDependencyAnalyzerApplication.class);
		assertDoesNotThrow(() -> {
			MavenDependencyAnalyzerApplication.class.getDeclaredMethod("main", String[].class);
		});
	}

}
