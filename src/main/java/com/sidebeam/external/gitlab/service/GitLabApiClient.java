package com.sidebeam.external.gitlab.service;

import com.sidebeam.external.gitlab.constant.GitLabApiConstants;
import com.sidebeam.external.gitlab.config.property.GitLabApiProperties;
import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.dto.GitLabFileDto;
import com.sidebeam.external.gitlab.dto.GitLabGroupDto;
import com.sidebeam.external.gitlab.dto.GitLabProjectDto;
import com.sidebeam.external.gitlab.dto.RepositoryFileDto;
import com.sidebeam.external.gitlab.util.GitLabApiPagingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * GitLab API와 통신하기 위한 클라이언트 컴포넌트입니다.
 * WebClient를 사용하여 GitLab API를 호출합니다.
 */
@Slf4j
@Component
public class GitLabApiClient {

    private final WebClient gitLabWebClient;
    private final GitLabProperties gitLabProperties;
    private final GitLabApiProperties apiProperties;

    public GitLabApiClient(WebClient.Builder webClientBuilder, GitLabProperties gitLabProperties, GitLabApiProperties apiProperties) {
        this.gitLabProperties = gitLabProperties;
        this.apiProperties = apiProperties;
        this.gitLabWebClient = webClientBuilder
                .baseUrl(gitLabProperties.getApiUrl())
                .defaultHeader("PRIVATE-TOKEN", gitLabProperties.getAccessToken())
                .build();
    }

    /**
     * GitLab API를 호출하여 그룹 정보를 가져옵니다.
     */
    public Mono<GitLabGroupDto> getGroup(String groupId) {
        log.debug("Fetching group info for groupId: {}", groupId);

        String path = apiProperties.getGroupEndpoints().getGet();
        return gitLabWebClient.get()
                .uri("/" + path, groupId)
                .retrieve()
                .bodyToMono(GitLabGroupDto.class)
                .doOnSuccess(response -> log.debug("Successfully fetched group info for groupId: {}", groupId))
                .doOnError(error -> log.error("Error fetching group info for groupId: {}", groupId, error));
    }

    /**
     * GitLab API를 호출하여 하위 그룹 목록을 가져옵니다.
     *
     * @param groupId 상위 그룹 ID
     * @return 하위 그룹 목록
     */
    public Flux<GitLabGroupDto> getSubgroups(String groupId) {
        log.debug("Fetching subgroups for groupId: {}", groupId);

        String path = apiProperties.getGroupEndpoints().getSubgroups();
        return gitLabWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + path)
                        .queryParam(GitLabApiConstants.PARAM_PER_PAGE, GitLabApiConstants.DEFAULT_PER_PAGE)
                        .build(groupId))
                .retrieve()
                .bodyToFlux(GitLabGroupDto.class)
                .doOnComplete(() -> log.debug("Successfully fetched subgroups for groupId: {}", groupId))
                .doOnError(error -> log.error("Error fetching subgroups for groupId: {}", groupId, error));
    }

    /**
     * GitLab API를 호출하여 그룹 내 프로젝트 목록을 가져옵니다.
     * 이 메서드는 하위 호환성을 위해 유지됩니다.
     */
    public Flux<GitLabProjectDto> getProjectIdListByGroupId(String groupId) {
        log.debug("Fetching projects for groupId: {}", groupId);

        String path = apiProperties.getGroupEndpoints().getProjects();
        
        return GitLabApiPagingUtils.paginateAll(page -> gitLabWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/" + path)
                                .queryParam(GitLabApiConstants.PARAM_PER_PAGE, GitLabApiConstants.DEFAULT_PER_PAGE)
                                .queryParam(GitLabApiConstants.PARAM_PAGE, page)
                                .queryParam(GitLabApiConstants.PARAM_INCLUDE_SUBGROUPS, true)
                                .build(groupId))
                        .retrieve()
                        .toEntityList(new ParameterizedTypeReference<List<GitLabProjectDto>>() {}))
                        .flatMap(Flux::fromIterable); // flatten List<GitLabProjectDto> → GitLabProjectDto
    }

    /**
     * GitLab API를 호출하여 프로젝트 내 파일 목록을 가져옵니다.
     */
    public Flux<GitLabFileDto> getRepositoryFiles(String projectId, String path) {
        log.debug("Fetching repository files for projectId: {}, path: {}", projectId, path);

        String apiPath = apiProperties.getProjectEndpoints().getRepository().getTree();

        return GitLabApiPagingUtils.paginateAll(page ->
                        gitLabWebClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .path("/" + apiPath)
                                        .queryParam(GitLabApiConstants.PARAM_PATH, path)
                                        .queryParam(GitLabApiConstants.PARAM_REF, gitLabProperties.getBranch())
                                        .queryParam(GitLabApiConstants.PARAM_PER_PAGE, GitLabApiConstants.DEFAULT_PER_PAGE)
                                        .queryParam(GitLabApiConstants.PARAM_PAGE, page)
                                        .build(projectId))
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response ->
                                        response.bodyToMono(String.class)
                                                .flatMap(body -> Mono.error(new RuntimeException("GitLab API error: " + body)))
                                )
                                .toEntityList(new ParameterizedTypeReference<List<GitLabFileDto>>() {})
                )
                .flatMap(Flux::fromIterable) // flatten List<GitLabFileDto> → GitLabFileDto
                .doOnComplete(() -> log.debug("Successfully fetched repository files for projectId: {}, path: {}", projectId, path))
                .doOnError(error -> log.error("Error fetching repository files for projectId: {}, path: {}", projectId, path, error));
    }

    /**
     * GitLab API를 호출하여 파일 내용을 가져옵니다.
     */
    public Mono<String> getFileContent(String projectId, String filePath) {
        log.debug("Fetching file content for projectId: {}, filePath: {}", projectId, filePath);

        String apiPath = apiProperties.getProjectEndpoints().getRepository().getFile().getRaw();
        return gitLabWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + apiPath)
                        .queryParam(GitLabApiConstants.PARAM_REF, gitLabProperties.getBranch())
                        .build(projectId, filePath))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.debug("Successfully fetched file content for projectId: {}, filePath: {}", projectId, filePath))
                .doOnError(error -> log.error("Error fetching file content for projectId: {}, filePath: {}", projectId, filePath, error));
    }
}