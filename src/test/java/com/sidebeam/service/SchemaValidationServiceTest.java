package com.sidebeam.service;

import com.sidebeam.bookmark.service.SchemaValidationService;
import com.sidebeam.bookmark.service.impl.SchemaValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SchemaValidationServiceTest {

    private SchemaValidationService schemaValidationService;

    @BeforeEach
    void setUp() {
        schemaValidationService = new SchemaValidationServiceImpl();
    }

    @Test
    void testValidYamlContent() {
        // Valid YAML content with all required fields
        String validYaml = """
            - name: Test Bookmark
              url: https://example.com
              domain: example.com
              category: Test/Category
              packages:
                - key: dev
                  children:
                    - key: docs
              meta:
                priority: 1
                owner: test-team
            """;

        // Should not throw an exception
        assertDoesNotThrow(() -> schemaValidationService.validateYamlContent(validYaml, "test.yml"));
    }

    @Test
    void testInvalidYamlContent_MissingRequiredField() {
        // Invalid YAML content missing required field (domain)
        String invalidYaml = """
            - name: Test Bookmark
              url: https://example.com
              category: Test/Category
            """;

        // Should throw an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> schemaValidationService.validateYamlContent(invalidYaml, "test.yml"));
        
        assertTrue(exception.getMessage().contains("domain"));
    }

    @Test
    void testInvalidYamlContent_InvalidCategoryFormat() {
        // Invalid YAML content with invalid category format
        String invalidYaml = """
            - name: Test Bookmark
              url: https://example.com
              domain: example.com
              category: InvalidCategory
            """;

        // Should throw an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> schemaValidationService.validateYamlContent(invalidYaml, "test.yml"));
        
        assertTrue(exception.getMessage().contains("category"));
    }

    @Test
    void testValidateAllYamlFiles() {
        Map<String, String> yamlFiles = new HashMap<>();
        
        // Add valid YAML content
        yamlFiles.put("valid.yml", """
            - name: Valid Bookmark
              url: https://example.com
              domain: example.com
              category: Test/Category
            """);
        
        // Should not throw an exception
        assertDoesNotThrow(() -> schemaValidationService.validateAllYamlFiles(yamlFiles));
        
        // Add invalid YAML content
        yamlFiles.put("invalid.yml", """
            - name: Invalid Bookmark
              url: https://example.com
              category: InvalidCategory
            """);
        
        // Should throw an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> schemaValidationService.validateAllYamlFiles(yamlFiles));
        
        assertTrue(exception.getMessage().contains("invalid.yml"));
    }
}