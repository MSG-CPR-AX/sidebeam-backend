package com.sidebeam.bookmark.service;

import com.sidebeam.external.gitlab.dto.AllFilesContentDto;
import java.io.IOException;

/**
 * Service for validating bookmark data against the JSON schema.
 */
public interface SchemaValidationService {

    /**
     * Validates a YAML content string against the bookmark schema.
     *
     * @param yamlContent The YAML content to validate
     * @param sourcePath The source path of the YAML file (for error reporting)
     * @throws IllegalArgumentException if the YAML content is invalid
     */
    void validateYamlContent(String yamlContent, String sourcePath);

    /**
     * Validates all YAML files in the given DTO against the bookmark schema.
     *
     * @param allFilesContent DTO containing file paths and YAML content
     * @throws IllegalArgumentException if any YAML file is invalid
     */
    void validateAllYamlFiles(AllFilesContentDto allFilesContent);

    /**
     * Loads the bookmark schema from the classpath.
     *
     * @return The schema as a string
     * @throws IOException if the schema file cannot be read
     */
    String loadBookmarkSchema() throws IOException;
}