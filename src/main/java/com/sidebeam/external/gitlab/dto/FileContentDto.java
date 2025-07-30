package com.sidebeam.external.gitlab.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 파일의 경로와 내용을 나타내는 DTO입니다.
 * Map.Entry<String, String> 대신 사용됩니다.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileContentDto {
    
    /**
     * 파일 경로
     */
    private String filePath;
    
    /**
     * 파일 내용
     */
    private String content;
}