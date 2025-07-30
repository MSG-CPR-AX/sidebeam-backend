package com.sidebeam.external.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitLabFileDto {

    /**
     * 파일 또는 디렉토리의 고유 ID
     */
    private String id;

    /**
     * 파일 또는 디렉토리 이름
     */
    private String name;

    /**
     * 파일 타입 (blob: 파일, tree: 디렉토리)
     * GitLab API에서 "type" 필드로 전달됨
     */
    @JsonProperty("type")
    private String type;

    /**
     * 파일 또는 디렉토리의 전체 경로
     */
    private String path;

    /**
     * 파일 모드 (권한 정보)
     */
    private String mode;

    /**
     * 웹 URL
     */
    @JsonProperty("web_url")
    private String webUrl;

    /**
     * 파일이 디렉토리인지 확인하는 편의 메서드
     *
     * @return 디렉토리인 경우 true, 파일인 경우 false
     */
    public boolean isDirectory() {
        return "tree".equals(type);
    }

    /**
     * 파일이 일반 파일인지 확인하는 편의 메서드
     *
     * @return 파일인 경우 true, 디렉토리인 경우 false
     */
    public boolean isFile() {
        return "blob".equals(type);
    }

    /**
     * 특정 확장자를 가진 파일인지 확인하는 편의 메서드
     *
     * @param extension 확인할 파일 확장자 (예: ".yml", ".yaml")
     * @return 해당 확장자를 가진 파일이면 true
     */
    public boolean hasExtension(String extension) {
        return isFile() && name != null && name.endsWith(extension);
    }

    /**
     * YAML 파일인지 확인하는 편의 메서드
     *
     * @return YAML 파일인 경우 true, 그렇지 않은 경우 false
     */
    public boolean isYamlFile() {
        if (!isFile()) {
            return false;
        }
        String lowerCaseName = name.toLowerCase();
        return lowerCaseName.endsWith(".yml") || lowerCaseName.endsWith(".yaml");
    }
}
