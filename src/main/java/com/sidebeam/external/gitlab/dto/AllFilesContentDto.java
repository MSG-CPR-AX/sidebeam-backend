package com.sidebeam.external.gitlab.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 모든 파일의 내용을 나타내는 DTO입니다.
 * Map<String, String> 대신 사용됩니다.
 */
public record AllFilesContentDto(
    /**
     * 파일 내용 목록
     */
    List<FileContentDto> fileContents
) {
    
    /**
     * 편의를 위한 Map 변환 메서드
     * 기존 코드와의 호환성을 위해 제공됩니다.
     */
    public Map<String, String> toMap() {
        return fileContents.stream()
                .collect(Collectors.toMap(
                    FileContentDto::filePath,
                    FileContentDto::content
                ));
    }
    
    /**
     * Map에서 AllFilesContentDto로 변환하는 정적 팩토리 메서드
     */
    public static AllFilesContentDto fromMap(Map<String, String> fileMap) {
        List<FileContentDto> fileContents = fileMap.entrySet().stream()
                .map(entry -> new FileContentDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return new AllFilesContentDto(fileContents);
    }
}