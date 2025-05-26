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
        List<String> filePaths = listYamlFiles();

        for (String filePath : filePaths) {
            String content = fetchYamlFile(filePath);
            if (content != null) {
                result.put(filePath, content);
            }
        }

        return result;
    }

    @Override
    public String fetchYamlFile(String filePath) {
        try {
            RepositoryFileApi repositoryApi = gitLabApi.getRepositoryFileApi();
            RepositoryFile file = repositoryApi.getFile(
                    gitLabProperties.getProjectId(),
                    filePath,
                    gitLabProperties.getBranch());

            if (file != null && file.getContent() != null) {
                byte[] decodedContent = Base64.getDecoder().decode(file.getContent());
                return new String(decodedContent);
            }
        } catch (GitLabApiException e) {
            log.error("Error fetching YAML file from GitLab: {}", filePath, e);
        }
        return null;
    }

    @Override
    public List<String> listYamlFiles() {
        List<String> yamlFiles = new ArrayList<>();
        try {
            RepositoryApi repositoryApi = gitLabApi.getRepositoryApi();
            String bookmarkDataPath = gitLabProperties.getBookmarkDataPath();
            String fileExtension = gitLabProperties.getFileExtension();

            // Get all files recursively
            List<TreeItem> items = repositoryApi.getTree(
                    gitLabProperties.getProjectId(),
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
            log.error("Error listing YAML files from GitLab", e);
        }
        return yamlFiles;
    }
}