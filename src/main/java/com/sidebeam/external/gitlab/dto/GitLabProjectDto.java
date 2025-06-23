package com.sidebeam.external.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record GitLabProjectDto(
        Long id,
        String description,
        @JsonProperty("default_branch")
        String defaultBranch,
        String visibility,
        @JsonProperty("ssh_url_to_repo")
        String sshUrlToRepo,
        @JsonProperty("http_url_to_repo")
        String httpUrlToRepo,
        @JsonProperty("web_url")
        String webUrl,
        @JsonProperty("readme_url")
        String readmeUrl,
        List<String> topics,
        Owner owner,
        String name,
        @JsonProperty("name_with_namespace")
        String nameWithNamespace,
        @JsonProperty("path")
        String path,
        @JsonProperty("path_with_namespace")
        String pathWithNamespace,
        @JsonProperty("created_at")
        OffsetDateTime createdAt,
        @JsonProperty("last_activity_at")
        OffsetDateTime lastActivityAt,
        ProjectStatistics statistics,
        @JsonProperty("container_registry_size")
        Long containerRegistrySize
) {
    public record Owner(
            Long id,
            String name,
            @JsonProperty("created_at")
            OffsetDateTime createdAt
    ) {}

    public record ProjectStatistics(
            @JsonProperty("storage_size")
            Long storageSize,
            @JsonProperty("repository_size")
            Long repositorySize,
            @JsonProperty("lfs_objects_size")
            Long lfsObjectsSize,
            @JsonProperty("job_artifacts_size")
            Long jobArtifactsSize,
            @JsonProperty("pipeline_artifacts_size")
            Long pipelineArtifactsSize
    ) {}
}
