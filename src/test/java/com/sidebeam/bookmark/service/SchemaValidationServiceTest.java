package com.sidebeam.bookmark.service;

import com.sidebeam.bookmark.config.property.ValidationProperties;
import com.sidebeam.bookmark.service.impl.SchemaValidationServiceImpl;
import com.sidebeam.external.gitlab.dto.AllFilesContentDto;
import com.sidebeam.external.gitlab.dto.FileContentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SchemaValidationServiceTest {

    private SchemaValidationService schemaValidationService;

    @BeforeEach
    void setUp() {
        ValidationProperties validationProperties = new ValidationProperties();
        validationProperties.getSchema().setStrict(true); // Enable strict validation for tests
        schemaValidationService = new SchemaValidationServiceImpl(validationProperties);
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

        // Should throw a domain ValidationException
        Exception exception = assertThrows(com.sidebeam.common.core.exception.ValidationException.class,
                () -> schemaValidationService.validateYamlContent(invalidYaml, "test.yml"));

        assertTrue(exception.getMessage().contains("domain") || exception.getMessage().contains("required"));
    }

    @Test
    void testInvalidYamlContent_InvalidCategoryFormat() {
        // Invalid YAML content with invalid category format (starts with slash)
        String invalidYaml = """
            - name: Test Bookmark
              url: https://example.com
              domain: example.com
              category: /InvalidCategory
            """;

        // Should throw a domain ValidationException
        Exception exception = assertThrows(com.sidebeam.common.core.exception.ValidationException.class,
                () -> schemaValidationService.validateYamlContent(invalidYaml, "test.yml"));

        assertTrue(exception.getMessage().contains("category") || exception.getMessage().contains("pattern"));
    }

    @Test
    void testValidateAllYamlFiles() {
        // Create valid YAML content
        FileContentDto validFile = new FileContentDto("valid.yml", """
            - name: Valid Bookmark
              url: https://example.com
              domain: example.com
              category: Test/Category
            """);

        AllFilesContentDto validFilesContent = new AllFilesContentDto(List.of(validFile));

        // Should not throw an exception
        assertDoesNotThrow(() -> schemaValidationService.validateAllYamlFiles(validFilesContent));

        // Add invalid YAML content (missing domain field)
        FileContentDto invalidFile = new FileContentDto("invalid.yml", """
            - name: Invalid Bookmark
              url: https://example.com
              category: Test/Category
            """);

        AllFilesContentDto mixedFilesContent = new AllFilesContentDto(List.of(validFile, invalidFile));

        // Should throw an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> schemaValidationService.validateAllYamlFiles(mixedFilesContent));

        assertTrue(exception.getMessage().contains("invalid.yml"));
    }
}
