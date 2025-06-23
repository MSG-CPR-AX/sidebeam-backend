package com.sidebeam.external.gitlab;

import com.sidebeam.external.gitlab.config.GitLabApiProperties;
import com.sidebeam.external.gitlab.config.GitLabProperties;
import com.sidebeam.external.gitlab.dto.GitLabGroupDto;
import com.sidebeam.external.gitlab.dto.GitLabProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

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
     *
     * @param groupId 그룹 ID
     * @return 그룹 정보
     */
    public Mono<GitLabGroupDto> getGroup(String groupId) {
        log.debug("Fetching group info for groupId: {}", groupId);

        String path = apiProperties.getGroups().getGet();
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

        String path = apiProperties.getGroups().getSubgroups();
        return gitLabWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + path)
                        .queryParam("per_page", 100)
                        .build(groupId))
                .retrieve()
                .bodyToFlux(GitLabGroupDto.class)
                .doOnComplete(() -> log.debug("Successfully fetched subgroups for groupId: {}", groupId))
                .doOnError(error -> log.error("Error fetching subgroups for groupId: {}", groupId, error));
    }

    /**
     * GitLab API를 호출하여 그룹 내 프로젝트 목록을 가져옵니다.
     *
     * @param groupId 그룹 ID
     * @return 프로젝트 목록
     */
    public Flux<GitLabProjectDto> getProjects(String groupId) {
        log.debug("Fetching projects for groupId: {}", groupId);

        String path = apiProperties.getGroups().getProjects();
        return gitLabWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + path)
                        .queryParam("per_page", 100)
                        .queryParam("include_subgroups", true)
                        .build(groupId))
                .retrieve()
                .bodyToFlux(GitLabProjectDto.class)
                .doOnComplete(() -> log.debug("Successfully fetched projects for groupId: {}", groupId))
                .doOnError(error -> log.error("Error fetching projects for groupId: {}", groupId, error));
    }

    /**
     * GitLab API를 호출하여 그룹 내 프로젝트 목록을 가져옵니다.
     * 이 메서드는 하위 호환성을 위해 유지됩니다.
     *
     * @param groupId 그룹 ID
     * @return 프로젝트 목록
     * @deprecated Use {@link #getProjects(String)} instead
     */
    @Deprecated
    public Flux<GitLabProjectDto> retrieveProjectIdListByGroupId(String groupId) {
        return getProjects(groupId);
    }

    /**
     * GitLab API를 호출하여 프로젝트 내 파일 목록을 가져옵니다.
     *
     * @param projectId 프로젝트 ID
     * @param path 파일 경로
     * @return 파일 목록
     */
    @SuppressWarnings("unchecked")
    public Flux<Map> getRepositoryFiles(String projectId, String path) {
        log.debug("Fetching repository files for projectId: {}, path: {}", projectId, path);

        String apiPath = apiProperties.getProjects().getRepository().getTree();
        return gitLabWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + apiPath)
                        .queryParam("path", path)
                        .queryParam("ref", gitLabProperties.getBranch())
                        .queryParam("per_page", 100)
                        .build(projectId))
                .retrieve()
                .bodyToFlux(Map.class)
                .doOnComplete(() -> log.debug("Successfully fetched repository files for projectId: {}, path: {}", projectId, path))
                .doOnError(error -> log.error("Error fetching repository files for projectId: {}, path: {}", projectId, path, error));
    }

    /**
     * GitLab API를 호출하여 파일 내용을 가져옵니다.
     *
     * @param projectId 프로젝트 ID
     * @param filePath 파일 경로
     * @return 파일 내용
     */
    public Mono<String> getFileContent(String projectId, String filePath) {
        log.debug("Fetching file content for projectId: {}, filePath: {}", projectId, filePath);

        String apiPath = apiProperties.getProjects().getRepository().getFile().getRaw();
        return gitLabWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + apiPath)
                        .queryParam("ref", gitLabProperties.getBranch())
                        .build(projectId, filePath))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.debug("Successfully fetched file content for projectId: {}, filePath: {}", projectId, filePath))
                .doOnError(error -> log.error("Error fetching file content for projectId: {}, filePath: {}", projectId, filePath, error));
    }

    /**
     * GitLab Open URL을 통해 파일 내용을 가져옵니다.
     *
     * @param projectId 프로젝트 ID
     * @param filePath 파일 경로
     * @return 파일 내용
     */
    public Mono<String> getFileContentViaOpenUrl(String projectId, String filePath) {
        String url = constructOpenUrl(projectId, filePath);
        log.debug("Fetching file content via open URL: {}", url);

        return WebClient.create()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.debug("Successfully fetched file content via open URL: {}", url))
                .doOnError(error -> log.error("Error fetching file content via open URL: {}", url, error));
    }

    /**
     * GitLab 저장소의 파일에 대한 open URL을 구성합니다.
     *
     * @param projectId GitLab 프로젝트 ID 또는 경로
     * @param filePath 저장소의 파일 경로
     * @return 파일의 open URL
     */
    private String constructOpenUrl(String projectId, String filePath) {
        String baseUrl = gitLabProperties.getApiUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String branch = gitLabProperties.getBranch();
        return String.format("%s/%s/-/raw/%s/%s", baseUrl, projectId, branch, filePath);
    }
}
