package com.sidebeam.external.gitlab.config.property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString(exclude = {"accessToken"})
@Configuration
@ConfigurationProperties(prefix = "gitlab")
public class GitLabProperties {

    private String apiUrl;

    @JsonIgnore
    private String accessToken;

    /**
     * 데이터 파일을 관리하는 루트 그룹의 ID입니다.
     * 이 ID를 통해 GitLab API를 호출하여 하위 그룹 및 프로젝트 목록을 모두 조회합니다.
     */
    private String rootGroupId;
    private String branch;
    private String bookmarkDataPath;
    private List<String> includedFileExtensions;
    /**
     * 제외할 파일명 목록 (예: README.md, LICENSE 등)
     */
    private List<String> excludedFilenames = new ArrayList<>();

    public boolean isIncludedFile(String filename) {
        return this.includedFileExtensions.stream().anyMatch(filename::endsWith);
    }

    public boolean isExcluded(String filename) {
        return this.excludedFilenames.contains(filename);
    }
}
