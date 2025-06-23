package com.sidebeam.external.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record GitLabGroupDto(
        Long id,
        String name,
        String path,
        String description,
        String visibility,

        @JsonProperty("avatar_url")
        String avatarUrl,

        @JsonProperty("web_url")
        String webUrl,

        @JsonProperty("request_access_enabled")
        Boolean requestAccessEnabled,

        @JsonProperty("repository_storage")
        String repositoryStorage,

        @JsonProperty("full_name")
        String fullName,

        @JsonProperty("full_path")
        String fullPath,

        @JsonProperty("file_template_project_id")
        Long fileTemplateProjectId,

        @JsonProperty("parent_id")
        Long parentId,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("runners_token")
        String runnersToken,

        @JsonProperty("enabled_git_access_protocol")
        String enabledGitAccessProtocol,

        GroupStatistics statistics,

        List<GitLabGroupDto> subgroups
) {
    public record GroupStatistics(
            @JsonProperty("storage_size")
            Long storageSize,

            @JsonProperty("repository_size")
            Long repositorySize,

            @JsonProperty("lfs_objects_size")
            Long lfsObjectsSize,

            @JsonProperty("job_artifacts_size")
            Long jobArtifactsSize,

            @JsonProperty("packages_size")
            Long packagesSize,

            @JsonProperty("snippets_size")
            Long snippetsSize,

            @JsonProperty("root_storage_statistics")
            RootStorageStatistics rootStorageStatistics
    ) {}

    public record RootStorageStatistics(
            @JsonProperty("storage_size")
            Long storageSize,

            @JsonProperty("repository_size")
            Long repositorySize,

            @JsonProperty("lfs_objects_size")
            Long lfsObjectsSize,

            @JsonProperty("job_artifacts_size")
            Long jobArtifactsSize
    ) {}
}
