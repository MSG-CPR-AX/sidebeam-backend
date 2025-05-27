package com.sidebar.service.impl;

import com.sidebar.config.GitLabProperties;
import com.sidebar.service.GitLabService;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.RepositoryFileApi;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.TreeItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GitLab 리포지토리와 상호 작용하기 위한 서비스입니다.
 * 이 서비스는 설정된 GitLab 프로젝트에서 YAML 파일을 검색, 목록화하고 가져오는 기능을 제공합니다.
 */
@Slf4j
@Service
public class GitLabServiceImpl implements GitLabService {

    private final GitLabProperties gitLabProperties;
    private final GitLabApi gitLabApi;

    public GitLabServiceImpl(GitLabProperties gitLabProperties) {
        this.gitLabProperties = gitLabProperties;
        this.gitLabApi = new GitLabApi(gitLabProperties.getApiUrl(), gitLabProperties.getAccessToken());
        this.gitLabApi.setIgnoreCertificateErrors(true); // SSL 인증서 검증 비활성화
    }

    @Override
    public Map<String, String> fetchAllYamlFiles() {
        Map<String, String> result = new HashMap<>();

        // Get the list of projects to fetch from
        List<String> projects = getProjectsList();

        for (String projectId : projects) {
            List<String> filePaths = listYamlFiles(projectId);

            for (String filePath : filePaths) {
                String content = fetchYamlFile(projectId, filePath);
                if (content != null) {
                    // Use project ID + file path as the key to avoid conflicts
                    String key = projectId + ":" + filePath;
                    result.put(key, content);
                }
            }
        }

        return result;
    }

    @Override
    public String fetchYamlFile(String filePath) {
        // For backward compatibility, use the default project ID
        return fetchYamlFile(gitLabProperties.getProjectId(), filePath);
    }

    /**
     * Fetches a YAML file from a specific GitLab project.
     *
     * @param projectId The GitLab project ID or path
     * @param filePath The path to the file in the repository
     * @return The content of the file, or null if it couldn't be fetched
     */
    private String fetchYamlFile(String projectId, String filePath) {
        try {
            RepositoryFileApi repositoryApi = gitLabApi.getRepositoryFileApi();
            RepositoryFile file = repositoryApi.getFile(
                    projectId,
                    filePath,
                    gitLabProperties.getBranch());

            if (file != null && file.getContent() != null) {
                byte[] decodedContent = Base64.getDecoder().decode(file.getContent());
                return new String(decodedContent);
            }
        } catch (GitLabApiException e) {
            log.error("Error fetching YAML file from GitLab project {}: {}", projectId, filePath, e);
        }
        return null;
    }

    @Override
    public List<String> listYamlFiles() {
        // For backward compatibility, use the default project ID
        return listYamlFiles(gitLabProperties.getProjectId());
    }

    /**
     * Lists all YAML files in a specific GitLab project.
     *
     * @param projectId The GitLab project ID or path
     * @return A list of file paths
     */
    private List<String> listYamlFiles(String projectId) {
        List<String> yamlFiles = new ArrayList<>();
        try {
            RepositoryApi repositoryApi = gitLabApi.getRepositoryApi();
            String bookmarkDataPath = gitLabProperties.getBookmarkDataPath();
            String fileExtension = gitLabProperties.getFileExtension();

            // Get all files recursively
            List<TreeItem> items = repositoryApi.getTree(
                    projectId,
                    bookmarkDataPath,
                    gitLabProperties.getBranch(),
                    true);

            // Filter for YAML files
            yamlFiles = items.stream()
                    .filter(item -> "blob".equals(item.getType()))
                    .filter(item -> item.getPath().endsWith(fileExtension))
                    .map(TreeItem::getPath)
                    .collect(Collectors.toList());
        } catch (GitLabApiException e) {
            log.error("Error listing YAML files from GitLab project {}", projectId, e);
        }
        return yamlFiles;
    }

    /**
     * Gets the list of GitLab projects to fetch bookmarks from.
     * If bookmarkProjects is empty, falls back to the single projectId.
     *
     * @return A list of project IDs or paths
     */
    private List<String> getProjectsList() {
        List<String> projects = gitLabProperties.getBookmarkProjects();

        // If no projects are configured, fall back to the single project ID
        if (projects == null || projects.isEmpty()) {
            String projectId = gitLabProperties.getProjectId();
            if (projectId != null && !projectId.isEmpty()) {
                projects = new ArrayList<>();
                projects.add(projectId);
            } else {
                log.warn("No GitLab projects configured for bookmark data");
                return new ArrayList<>();
            }
        }

        return projects;
    }
}
