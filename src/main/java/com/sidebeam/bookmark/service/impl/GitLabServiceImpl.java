package com.sidebeam.bookmark.service.impl;

import com.sidebeam.external.gitlab.service.GitLabApiClient;
import com.sidebeam.external.gitlab.service.GitLabStorageFileRetriever;
import com.sidebeam.common.cache.component.SpringCacheManager;
import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.dto.AllFilesContentDto;
import com.sidebeam.bookmark.service.GitLabService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

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
@RequiredArgsConstructor
public class GitLabServiceImpl implements GitLabService {

    private final GitLabProperties gitLabProperties;
    private final GitLabApiClient gitLabApiClient;
    private final GitLabStorageFileRetriever fileRetriever;  // 새로 추가
    private final SpringCacheManager springCacheManager;

    public AllFilesContentDto retrieveAllYamlFiles() {
        return springCacheManager.getCachedData(AllFilesContentDto.class)
                .switchIfEmpty(this.retrieveAndCacheAllYamlFiles())
                .block();
    }

    @SuppressWarnings("deprecation")
    private Mono<AllFilesContentDto> retrieveAndCacheAllYamlFiles() {
        log.debug("GitLab API를 통해 모든 YAML 파일 가져오기");

        String rootGroupId = gitLabProperties.getRootGroupId();
        if (StringUtils.isEmpty(rootGroupId)) {
            log.error("루트 그룹 ID가 설정되지 않았습니다");
            return Mono.just(new AllFilesContentDto(new ArrayList<>()));
        }

        return gitLabApiClient
                // 그룹 하위 프로젝트 목록 조회(서브그룹의 프로젝트 포함)
                .getProjectIdListByGroupId(rootGroupId)
                // 프로젝트 내부 파일 목록 조회
                .flatMap(fileRetriever::retrieveProjectFilePaths)
                .collectList()
                .flatMap(fileRetriever::retrieveFileContents) // 위임
                .flatMap(springCacheManager::cacheData);
    }
}
