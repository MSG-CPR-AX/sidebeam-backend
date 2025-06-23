package com.sidebeam.bookmark.service.impl;

import com.sidebeam.external.gitlab.GitLabApiClient;
import com.sidebeam.external.gitlab.GitLabStorageFileRetriever;
import com.sidebeam.bookmark.component.SpringCacheManager;
import com.sidebeam.external.gitlab.config.GitLabProperties;
import com.sidebeam.bookmark.service.GitLabService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitLab 리포지토리와 상호 작용하기 위한 서비스입니다.
 * 이 서비스는 GitLab API를 호출하여 하위 그룹 및 프로젝트 목록을 조회하고,
 * 각 프로젝트의 데이터 파일을 가져와 하나의 JSON으로 집계합니다.
 * 
 * 이 구현은 WebClient를 사용하여 GitLab API를 호출하며,
 * 컴포넌트 기반 설계(CBD)를 따라 기능별로 컴포넌트화되어 있습니다.
 */
@Slf4j
@Service
public class GitLabServiceImpl implements GitLabService {

    private final GitLabProperties gitLabProperties;
    private final GitLabApiClient gitLabApiClient;
    private final GitLabStorageFileRetriever fileRetriever;  // 새로 추가
    private final SpringCacheManager springCacheManager;

    public GitLabServiceImpl(GitLabProperties gitLabProperties,
                            GitLabApiClient gitLabApiClient,
                            GitLabStorageFileRetriever fileRetriever,
                            SpringCacheManager springCacheManager) {
        this.gitLabProperties = gitLabProperties;
        this.gitLabApiClient = gitLabApiClient;
        this.fileRetriever = fileRetriever;
        this.springCacheManager = springCacheManager;
    }

    @Override
    public Map<String, String> fetchAllYamlFiles() {
        return springCacheManager.getCachedData(Map.class)
                .switchIfEmpty(this.fetchAndCacheAllYamlFiles())
                .block();
    }

    private Mono<Map<String, String>> fetchAndCacheAllYamlFiles() {
        log.info("GitLab API를 통해 모든 YAML 파일 가져오기");

        String rootGroupId = gitLabProperties.getRootGroupId();
        if (rootGroupId == null || rootGroupId.isEmpty()) {
            log.error("루트 그룹 ID가 설정되지 않았습니다");
            return Mono.just(new HashMap<>());
        }

        return gitLabApiClient.retrieveProjectIdListByGroupId(rootGroupId)
                .flatMap(fileRetriever::getProjectFiles)  // 위임
                .collectList()
                .map(fileRetriever::mergeProjectFiles)    // 위임
                .flatMap(fileRetriever::fetchFileContents) // 위임
                .flatMap(springCacheManager::cacheData);
    }

    @Override
    public String fetchYamlFile(String filePath) {
        return fileRetriever.fetchSingleFileContent(
                        gitLabProperties.getProjectId(), filePath)
                .block();
    }

    @Override
    public List<String> listYamlFiles() {
        return fileRetriever.listProjectFiles(gitLabProperties.getProjectId())
                .collectList()
                .block();
    }
}
