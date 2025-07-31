package com.sidebeam.external.gitlab.dto;

/**
 * 파일의 경로와 내용을 나타내는 DTO입니다.
 * Map.Entry<String, String> 대신 사용됩니다.
 */
public record FileContentDto(
    /**
     * 파일 경로
     */
    String filePath,
    
    /**
     * 파일 내용
     */
    String content
) {}