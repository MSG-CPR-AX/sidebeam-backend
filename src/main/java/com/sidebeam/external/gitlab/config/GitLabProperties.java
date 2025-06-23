package com.sidebeam.external.gitlab.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "gitlab")
public class GitLabProperties {

    private String apiUrl;
    private String accessToken;
    private String projectId; // Kept for backward compatibility

    /**
     * 데이터 파일을 관리하는 루트 그룹의 ID입니다.
     * 이 ID를 통해 GitLab API를 호출하여 하위 그룹 및 프로젝트 목록을 모두 조회합니다.
     */
    private String rootGroupId;

    /**
     * 북마크 데이터를 가져올 GitLab 프로젝트와 그룹의 목록입니다.
     * 이 목록에는 프로젝트 ID뿐만 아니라 그룹 ID도 포함될 수 있으며,
     * 이를 통해 GitLab의 다양한 레벨(상위 그룹, 하위 그룹, 프로젝트 등)에서
     * 북마크 데이터를 가져올 수 있습니다.
     */
    private List<String> bookmarkProjects = new ArrayList<>();
    private String branch;
    private String bookmarkDataPath;
    private String fileExtension;
}
