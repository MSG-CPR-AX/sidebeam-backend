package com.sidebar.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sidebar.service.SchemaValidationService;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Implementation of the SchemaValidationService.
 */
@Slf4j
@Service
public class SchemaValidationServiceImpl implements SchemaValidationService {

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public SchemaValidationServiceImpl() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public void validateYamlContent(String yamlContent, String sourcePath) {
        try {
            // Load the schema
            String schemaContent = loadBookmarkSchema();
            JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaContent));
            Schema schema = SchemaLoader.load(jsonSchema);

            // Convert YAML to JSON
            JsonNode yamlNode = yamlMapper.readTree(yamlContent);
            String jsonContent = jsonMapper.writeValueAsString(yamlNode);
            JSONArray jsonArray = new JSONArray(new JSONTokener(jsonContent));

            // Validate
            try {
                schema.validate(jsonArray);
                log.info("Schema validation passed for {}", sourcePath);
            } catch (ValidationException e) {
                StringBuilder errorMessage = new StringBuilder();
                errorMessage.append("Schema validation failed for ").append(sourcePath).append(":\n");
                
                e.getCausingExceptions().stream()
                        .map(ValidationException::getMessage)
                        .forEach(msg -> errorMessage.append("- ").append(msg).append("\n"));
                
                log.error(errorMessage.toString());
                throw new IllegalArgumentException(errorMessage.toString(), e);
            }
        } catch (IOException e) {
            log.error("Error loading schema or parsing YAML: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error loading schema or parsing YAML", e);
        }
    }

    @Override
    public void validateAllYamlFiles(Map<String, String> yamlFiles) {
        boolean hasErrors = false;
        StringBuilder errorMessages = new StringBuilder();

        for (Map.Entry<String, String> entry : yamlFiles.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();
            
            try {
                validateYamlContent(content, filePath);
            } catch (IllegalArgumentException e) {
                hasErrors = true;
                errorMessages.append(e.getMessage()).append("\n");
            }
        }

        if (hasErrors) {
            throw new IllegalArgumentException("Schema validation failed for one or more files:\n" + errorMessages);
        }
    }

    @Override
    public String loadBookmarkSchema() throws IOException {
        try (InputStream inputStream = new ClassPathResource("bookmark-schema/bookmark.schema.json").getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}