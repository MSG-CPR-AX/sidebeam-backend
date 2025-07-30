package com.sidebeam.external.gitlab.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 프로젝트의 파일 목록을 나타내는 DTO입니다.
 * Map<String, List<String>> 대신 사용됩니다.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFilesDto {
    
    /**
     * 프로젝트 ID
     */
    private String projectId;
    
    /**
     * 프로젝트 내 파일 경로 목록
     */
    private List<String> filePaths;
}