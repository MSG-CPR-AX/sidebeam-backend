package com.sidebeam.external.gitlab.service;

import com.sidebeam.common.core.exception.ErrorCode;
import com.sidebeam.common.core.exception.TechnicalException;
import com.sidebeam.external.gitlab.config.property.GitLabApiProperties;
import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.constant.GitLabApiConstants;
import com.sidebeam.external.gitlab.dto.GitLabFileDto;
import com.sidebeam.external.gitlab.dto.GitLabGroupDto;
import com.sidebeam.external.gitlab.dto.GitLabProjectDto;
import com.sidebeam.external.gitlab.util.GitLabApiPagingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private final RetryPolicy retryPolicy;

    public GitLabApiClient(WebClient.Builder webClientBuilder,
                           GitLabProperties gitLabProperties,
                           GitLabApiProperties apiProperties,
                           RetryPolicy retryPolicy) {
        this.gitLabProperties = gitLabProperties;
        this.apiProperties = apiProperties;
        this.retryPolicy = retryPolicy;

        // 공통 WebClient 설정(WebClientConfig) 사용: 타임아웃/커넥션 풀/SSL 검증은 글로벌 설정에 따름
        this.gitLabWebClient = webClientBuilder
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
     *
     * <p>Resilience4j 애노테이션 대신 {@link RetryPolicy}를 사용하여
     * 재시도와 오류 매핑을 일관되게 적용합니다. 오류는 내부 mapError() 메서드를 통해 도메인 예외로 변환됩니다.</p>
     */
    public Mono<GitLabGroupDto> getGroup(String groupId) {
        log.debug("Fetching group info for groupId: {}", groupId);

        String path = apiProperties.getGroupEndpoints().getGet();
        Mono<GitLabGroupDto> call = gitLabWebClient.get()
                .uri("/" + path, groupId)
                .retrieve()
                .bodyToMono(GitLabGroupDto.class)
                .doOnSuccess(response -> log.debug("Successfully fetched group info for groupId: {}", groupId))
                .doOnError(error -> log.error("Error fetching group info for groupId: {}", groupId, error));

        return retryPolicy.withRetry(call, "getGroup")
                .onErrorMap(throwable -> mapError("getGroup", throwable));
    }

    // Resilience4j용 fallback 메서드는 삭제되었습니다. 오류 처리는 내부 mapError()에서 처리합니다.

    /**
     * GitLab API를 호출하여 하위 그룹 목록을 가져옵니다.
     *
     * @param groupId 상위 그룹 ID
     * @return 하위 그룹 목록
     */
    public Flux<GitLabGroupDto> getSubgroups(String groupId) {
        log.debug("Fetching subgroups for groupId: {}", groupId);

        String path = apiProperties.getGroupEndpoints().getSubgroups();
        Flux<GitLabGroupDto> call = gitLabWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + path)
                        .queryParam(GitLabApiConstants.PARAM_PER_PAGE, GitLabApiConstants.DEFAULT_PER_PAGE)
                        .build(groupId))
                .retrieve()
                .bodyToFlux(GitLabGroupDto.class)
                .doOnComplete(() -> log.debug("Successfully fetched subgroups for groupId: {}", groupId))
                .doOnError(error -> log.error("Error fetching subgroups for groupId: {}", groupId, error));
        return retryPolicy.withRetry(call, "getSubgroups")
                .onErrorMap(throwable -> mapError("getSubgroups", throwable));
    }

    // Resilience4j용 fallback 메서드는 삭제되었습니다. 오류 처리는 내부 mapError()에서 처리합니다.

    /**
     * GitLab API를 호출하여 그룹 내 프로젝트 목록을 가져옵니다.
     * 이 메서드는 하위 호환성을 위해 유지됩니다.
     */
    public Flux<GitLabProjectDto> getProjectIdListByGroupId(String groupId) {

        log.debug("Fetching projects for groupId: {}", groupId);

        String path = apiProperties.getGroupEndpoints().getProjects();

        Flux<GitLabProjectDto> call = GitLabApiPagingUtils.paginateAll(page -> gitLabWebClient.get()
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
                .doOnError(error -> log.error("전체 프로젝트 조회 실패 - groupId: {}", groupId, error));

        return retryPolicy.withRetry(call, "getProjectIdListByGroupId")
                .onErrorMap(throwable -> mapError("getProjectIdListByGroupId", throwable));
    }

    // Resilience4j용 fallback 메서드는 삭제되었습니다. 오류 처리는 내부 mapError()에서 처리합니다.

    /**
     * GitLab API를 호출하여 프로젝트 내 파일 목록을 가져옵니다.
     */
    public Flux<GitLabFileDto> getRepositoryFiles(String projectId, String path) {
        log.debug("Fetching repository files for projectId: {}, path: {}", projectId, path);

        String apiPath = apiProperties.getProjectEndpoints().getRepository().getTree();

        Flux<GitLabFileDto> call = GitLabApiPagingUtils.paginateAll(page ->
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
                                .toEntityList(new ParameterizedTypeReference<List<GitLabFileDto>>() {}))
                .flatMap(Flux::fromIterable) // flatten List<GitLabFileDto> → GitLabFileDto
                .doOnComplete(() -> log.debug("Successfully fetched repository files for projectId: {}, path: {}", projectId, path))
                .doOnError(error -> log.error("Error fetching repository files for projectId: {}, path: {}", projectId, path, error));
        return retryPolicy.withRetry(call, "getRepositoryFiles")
                .onErrorMap(throwable -> mapError("getRepositoryFiles", throwable));
    }

    // Resilience4j용 fallback 메서드는 삭제되었습니다. 오류 처리는 내부 mapError()에서 처리합니다.

    /**
     * GitLab API를 호출하여 파일 내용을 가져옵니다.
     */
    public Mono<String> getFileContent(String projectId, String filePath) {
        log.debug("Fetching file content for projectId: {}, filePath: {}", projectId, filePath);

        String apiPath = apiProperties.getProjectEndpoints().getRepository().getFile().getRaw();
        Mono<String> call = gitLabWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + apiPath)
                        .queryParam(GitLabApiConstants.PARAM_REF, gitLabProperties.getBranch())
                        .build(projectId, filePath))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.debug("Successfully fetched file content for projectId: {}, filePath: {}", projectId, filePath))
                .doOnError(error -> log.error("Error fetching file content for projectId: {}, filePath: {}", projectId, filePath, error));
        return retryPolicy.withRetry(call, "getFileContent")
                .onErrorMap(throwable -> mapError("getFileContent", throwable));
    }

    // Resilience4j용 fallback 메서드는 삭제되었습니다. 오류 처리는 내부 mapError()에서 처리합니다.

    /**
     * 외부 호출에서 발생한 예외를 공통 도메인 예외로 변환합니다.
     * 특정 컨텍스트 정보를 포함하여 로그를 남기고 {@link TechnicalException}으로 감싸 반환합니다.
     *
     * @param context 호출 중인 메서드명 등 컨텍스트 식별자
     * @param throwable 발생한 원본 예외
     * @return 도메인 계층에서 사용할 RuntimeException
     */
    private RuntimeException mapError(String context, Throwable throwable) {
        log.error("[External] {} 실패: {}", context, throwable.getMessage(), throwable);
        return new TechnicalException(ErrorCode.GITLAB_API_ERROR, "외부 시스템 호출 실패(" + context + ")", throwable);
    }
}
