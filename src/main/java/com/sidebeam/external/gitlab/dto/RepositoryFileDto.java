package com.sidebeam.external.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GitLab Repository Tree API 응답을 나타내는 DTO 클래스
 *
 * GitLab API의 /projects/:id/repository/tree 엔드포인트 응답 구조에 맞춰 설계되었습니다.
 *
 * @see <a href="https://docs.gitlab.com/ee/api/repositories.html#list-repository-tree">GitLab Repository Tree API</a>
 */
public record RepositoryFileDto(
    /**
     * 파일 또는 디렉토리 ID
     */
    String id,

    /**
     * 파일 또는 디렉토리 이름
     */
    String name,

    /**
     * 파일 타입 (blob: 파일, tree: 디렉토리)
     */
    String type,

    /**
     * 파일 또는 디렉토리 경로
     */
    String path,

    /**
     * 파일 모드 (권한)
     */
    String mode,

    /**
     * 웹 URL
     */
    @JsonProperty("web_url")
    String webUrl
) {

    /**
     * 파일인지 확인하는 편의 메서드
     */
    public boolean isFile() {
        return "blob".equals(type);
    }

    /**
     * 디렉토리인지 확인하는 편의 메서드
     */
    public boolean isDirectory() {
        return "tree".equals(type);
    }

    /**
     * 특정 확장자를 가진 파일인지 확인하는 편의 메서드
     */
    public boolean hasExtension(String extension) {
        return isFile() && name != null && name.endsWith(extension);
    }
}
