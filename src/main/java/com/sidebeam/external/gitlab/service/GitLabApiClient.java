package com.sidebeam.external.gitlab.service;

import com.sidebeam.common.core.exception.ErrorCode;
import com.sidebeam.common.core.exception.TechnicalException;
import com.sidebeam.external.gitlab.constant.GitLabApiConstants;
import com.sidebeam.external.gitlab.config.property.GitLabApiProperties;
import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.dto.GitLabFileDto;
import com.sidebeam.external.gitlab.dto.GitLabGroupDto;
import com.sidebeam.external.gitlab.dto.GitLabProjectDto;
import com.sidebeam.external.gitlab.dto.RepositoryFileDto;
import com.sidebeam.external.gitlab.util.GitLabApiPagingUtils;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.List;

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

        // SSL 검증을 비활성화한 HttpClient 생성 + 타임아웃 구성
        HttpClient httpClient = HttpClient.create()
                .wiretap(true) // 네트워크 레벨 로깅 활성화
                .secure(sslSpec -> {
                    try {
                        sslSpec.sslContext(
                                SslContextBuilder.forClient()
                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                        .build()
                        );
                    } catch (SSLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(5));

        this.gitLabWebClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(gitLabProperties.getApiUrl())
                .defaultHeader("PRIVATE-TOKEN", gitLabProperties.getAccessToken())
                .filter((request, next) -> {
                    // 요청 URL 로깅
                    log.debug("GitLab API 요청: {} {}", request.method(), request.url());
                    return next.exchange(request);
                })
                .build();
    }

    /**
     * GitLab API를 호출하여 그룹 정보를 가져옵니다.
     */
    @CircuitBreaker(name = "gitlab", fallbackMethod = "fallbackGetGroup")
    @Retry(name = "gitlab")
    @Bulkhead(name = "gitlab")
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

    private Mono<GitLabGroupDto> fallbackGetGroup(String groupId, Throwable t) {
        log.error("[CB][Fallback] getGroup failed for groupId: {}", groupId, t);
        return Mono.error(new TechnicalException(ErrorCode.GITLAB_API_ERROR, "Failed to fetch group info", t));
    }

    /**
     * GitLab API를 호출하여 하위 그룹 목록을 가져옵니다.
     *
     * @param groupId 상위 그룹 ID
     * @return 하위 그룹 목록
     */
    @CircuitBreaker(name = "gitlab", fallbackMethod = "fallbackGetSubgroups")
    @Retry(name = "gitlab")
    @Bulkhead(name = "gitlab")
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

    private Flux<GitLabGroupDto> fallbackGetSubgroups(String groupId, Throwable t) {
        log.error("[CB][Fallback] getSubgroups failed for groupId: {}", groupId, t);
        return Flux.error(new TechnicalException(ErrorCode.GITLAB_API_ERROR, "Failed to fetch subgroups", t));
    }

    /**
     * GitLab API를 호출하여 그룹 내 프로젝트 목록을 가져옵니다.
     * 이 메서드는 하위 호환성을 위해 유지됩니다.
     */
    @CircuitBreaker(name = "gitlab", fallbackMethod = "fallbackGetProjects")
    @Retry(name = "gitlab")
    @Bulkhead(name = "gitlab")
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
                        .toEntityList(new ParameterizedTypeReference<GitLabProjectDto>() {}))
                .doOnNext(project -> log.debug("프로젝트 수신: ID={}, Name={}", project.id(), project.name()))
                .doOnComplete(() -> log.debug("모든 프로젝트 조회 완료 - groupId: {}", groupId))
                .doOnError(error -> log.error("전체 프로젝트 조회 실패 - groupId: {}", groupId, error)); // flatten List<GitLabProjectDto> → GitLabProjectDto
    }

    private Flux<GitLabProjectDto> fallbackGetProjects(String groupId, Throwable t) {
        log.error("[CB][Fallback] getProjectIdListByGroupId failed for groupId: {}", groupId, t);
        return Flux.error(new TechnicalException(ErrorCode.GITLAB_API_ERROR, "Failed to fetch projects", t));
    }

    /**
     * GitLab API를 호출하여 프로젝트 내 파일 목록을 가져옵니다.
     */
    @CircuitBreaker(name = "gitlab", fallbackMethod = "fallbackGetRepositoryFiles")
    @Retry(name = "gitlab")
    @Bulkhead(name = "gitlab")
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

    private Flux<GitLabFileDto> fallbackGetRepositoryFiles(String projectId, String path, Throwable t) {
        log.error("[CB][Fallback] getRepositoryFiles failed for projectId: {}, path: {}", projectId, path, t);
        return Flux.error(new TechnicalException(ErrorCode.GITLAB_API_ERROR, "Failed to fetch repository files", t));
    }

    /**
     * GitLab API를 호출하여 파일 내용을 가져옵니다.
     */
    @CircuitBreaker(name = "gitlab", fallbackMethod = "fallbackGetFileContent")
    @Retry(name = "gitlab")
    @Bulkhead(name = "gitlab")
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

    private Mono<String> fallbackGetFileContent(String projectId, String filePath, Throwable t) {
        log.error("[CB][Fallback] getFileContent failed for projectId: {}, filePath: {}", projectId, filePath, t);
        return Mono.error(new TechnicalException(ErrorCode.GITLAB_API_ERROR, "Failed to fetch file content", t));
    }
}