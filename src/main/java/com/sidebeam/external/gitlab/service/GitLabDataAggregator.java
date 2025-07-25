package com.sidebeam.external.gitlab.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitLab 데이터 파일들을 하나의 JSON으로 집계하는 컴포넌트입니다.
 */
@Slf4j
@Component
public class GitLabDataAggregator {

    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;

    public GitLabDataAggregator() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * 여러 YAML 파일의 내용을 하나의 JSON으로 집계합니다.
     *
     * @param fileContents 파일 경로와 내용의 맵
     * @return 집계된 JSON 문자열
     */
    public Mono<String> aggregateToJson(Map<String, String> fileContents) {
        return Mono.fromCallable(() -> {
            Map<String, Object> aggregatedData = new HashMap<>();
            
            for (Map.Entry<String, String> entry : fileContents.entrySet()) {
                String filePath = entry.getKey();
                String content = entry.getValue();
                
                try {
                    // YAML 내용을 파싱하여 객체로 변환
                    List<?> parsedData = yamlMapper.readValue(content, 
                            yamlMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
                    
                    // 파일 경로를 키로 사용하여 집계 맵에 추가
                    aggregatedData.put(filePath, parsedData);
                    log.debug("Aggregated data from file: {}", filePath);
                } catch (IOException e) {
                    log.error("Error parsing YAML content from file: {}", filePath, e);
                }
            }
            
            // 집계된 데이터를 JSON 문자열로 변환
            return jsonMapper.writeValueAsString(aggregatedData);
        });
    }
    
    /**
     * 여러 YAML 파일의 내용을 하나의 객체로 집계합니다.
     *
     * @param fileContents 파일 경로와 내용의 맵
     * @return 집계된 데이터 객체
     */
    public Mono<Map<String, Object>> aggregateToMap(Map<String, String> fileContents) {
        return Mono.fromCallable(() -> {
            Map<String, Object> aggregatedData = new HashMap<>();
            
            for (Map.Entry<String, String> entry : fileContents.entrySet()) {
                String filePath = entry.getKey();
                String content = entry.getValue();
                
                try {
                    // YAML 내용을 파싱하여 객체로 변환
                    List<?> parsedData = yamlMapper.readValue(content, 
                            yamlMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
                    
                    // 파일 경로를 키로 사용하여 집계 맵에 추가
                    aggregatedData.put(filePath, parsedData);
                    log.debug("Aggregated data from file: {}", filePath);
                } catch (IOException e) {
                    log.error("Error parsing YAML content from file: {}", filePath, e);
                }
            }
            
            return aggregatedData;
        });
    }
}