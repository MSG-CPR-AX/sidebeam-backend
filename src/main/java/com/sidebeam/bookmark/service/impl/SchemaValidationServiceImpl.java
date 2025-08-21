package com.sidebeam.bookmark.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sidebeam.bookmark.config.property.ValidationProperties;
import com.sidebeam.bookmark.service.SchemaValidationService;
import com.sidebeam.external.gitlab.dto.AllFilesContentDto;
import com.sidebeam.external.gitlab.dto.FileContentDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

/**
 * Implementation of the SchemaValidationService.
 */
@Slf4j
@Service
public class SchemaValidationServiceImpl implements SchemaValidationService {

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;
    private final ValidationProperties.Schema schemaValidationProperties;

    public SchemaValidationServiceImpl(ValidationProperties validationProperties) {
        this.schemaValidationProperties = validationProperties.getSchema();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }

    @Override
    public void validateYamlContent(String yamlContent, String sourcePath) {
        // 스키마 검증이 비활성화된 경우 바로 리턴
        if (!schemaValidationProperties.isEnabled()) {
            log.info("Schema validation is disabled, skipping validation for {}", sourcePath);
            return;
        }

        try {
            // 프로퍼티에서 스키마 경로를 가져와서 로드
            String schemaContent = this.loadBookmarkSchema();
            JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaContent));
            Schema schema = SchemaLoader.load(jsonSchema);

            // Convert YAML to JSON
            JsonNode yamlNode = yamlMapper.readTree(yamlContent);
            String jsonContent = jsonMapper.writeValueAsString(yamlNode);
            JSONArray jsonArray = new JSONArray(new JSONTokener(jsonContent));

            // Validate
            try {
                schema.validate(jsonArray);
                log.debug("Schema validation passed for {}", sourcePath);
            } catch (ValidationException e) {
                // 엄격한 검증 모드인 경우에만 예외 발생
                if (!schemaValidationProperties.isStrict()) {
                    log.warn("Schema validation failed but continuing due to non-strict validation mode");
                    return;
                }

                // getAllMessages()를 활용한 간소화된 에러 메시지 생성
                String errorMessage = StringUtils.join( "Schema validation failed for ", sourcePath, ":\n",
                        String.join("\n", e.getAllMessages()));

                log.error(errorMessage);
                throw new com.sidebeam.common.core.exception.ValidationException(
                        com.sidebeam.common.core.exception.ErrorCode.SCHEMA_VALIDATION_ERROR,
                        errorMessage,
                        e
                );
            }
        } catch (IOException e) {
            log.error("Error loading schema or parsing YAML: {}", e.getMessage(), e);

            // 엄격한 검증 모드인 경우에만 예외 발생
            if (schemaValidationProperties.isStrict()) {
                throw new com.sidebeam.common.core.exception.ValidationException(
                        com.sidebeam.common.core.exception.ErrorCode.SCHEMA_VALIDATION_ERROR,
                        "Error loading schema or parsing YAML",
                        e
                );
            } else {
                log.warn("Schema loading failed but continuing due to non-strict validation mode");
            }
        }
    }

    @Override
    public void validateAllYamlFiles(AllFilesContentDto allFilesContent) {
        // 스키마 검증이 비활성화된 경우 바로 리턴
        if (!schemaValidationProperties.isEnabled()) {
            log.debug("Schema validation is disabled, skipping validation for all files");
            return;
        }

        boolean hasErrors = false;
        StringBuilder errorMessages = new StringBuilder();

        for (FileContentDto fileContent : allFilesContent.fileContents()) {
            String filePath = fileContent.filePath();
            String content = fileContent.content();

            try {
                this.validateYamlContent(content, filePath);
            } catch (com.sidebeam.common.core.exception.ValidationException e) {
                hasErrors = true;
                errorMessages.append(e.getMessage()).append("\n");
            }
        }

        if (hasErrors && schemaValidationProperties.isStrict()) {
            // 테스트 계약에 따라 혼합 유효/무효 파일의 배치 검증 실패 시 IllegalArgumentException을 던집니다.
            throw new IllegalArgumentException("Schema validation failed for one or more files:\n" + errorMessages);
        } else if (hasErrors) {
            log.warn("Schema validation failed for some files but continuing due to non-strict validation mode");
        }
    }

    @Override
    public String loadBookmarkSchema() throws IOException {

        String schemaPath = schemaValidationProperties.getBookmarkSchemaPath();
        log.debug("Loading bookmark schema from path: {}", schemaPath);

        try (InputStream inputStream = new ClassPathResource(schemaPath).getInputStream()) {

            String schema = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.debug("Successfully loaded bookmark schema from: {}", schemaPath);

            return schema;

        } catch (IOException e) {
            log.error("Failed to load bookmark schema from path: {}", schemaPath, e);
            throw new IOException("Failed to load bookmark schema from: " + schemaPath, e);
        }
    }
}
