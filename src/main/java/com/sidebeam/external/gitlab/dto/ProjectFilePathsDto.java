package com.sidebeam.external.gitlab.dto;

import java.util.List;

/**
 * 프로젝트의 파일 목록을 나타내는 DTO입니다.
 * Map<String, List<String>> 대신 사용됩니다.
 */
public record ProjectFilePathsDto(
    /**
     * 프로젝트 ID
     */
    String projectId,
    
    /**
     * 프로젝트 내 파일 경로 목록
     */
    List<String> filePaths
) {}