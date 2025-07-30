
package com.sidebeam.external.gitlab.service;

import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GitLab 저장소에서 파일을 조회하는 전용 컴포넌트입니다.
 * 파일 목록 조회, 파일 내용 조회, 디렉토리 탐색 등의 기능을 제공합니다.
 *
 * File List -> Defined Class
 * File Contents -> Law 타입
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitLabStorageFileRetriever {

    private final GitLabProperties gitLabProperties;
    private final GitLabApiClient gitLabApiClient;

    /**
     * 프로젝트에서 YAML 파일 목록을 가져옵니다.
     */
    public Mono<ProjectFilesDto> retrieverProjectFiles(GitLabProjectDto project) {
        String projectId = project.id().toString();
        String projectPath = project.pathWithNamespace();
        
        log.info("프로젝트 {}의 파일 목록 가져오기", projectPath);

        return gitLabApiClient.getRepositoryFiles(projectId, projectPath)
                .filter(fileDto -> {
                    String name = (String) fileDto.getName();
                    return name.endsWith(gitLabProperties.getFileExtension()) || isDirectory(fileDto.getType());
                })
                .flatMap(fileDto -> {
                    if (isDirectory(fileDto.getType())) {
                        return this.retrieveFilesInDirectory(projectId, fileDto.getPath());
                    } else {
                        return Flux.just((String) fileDto.getPath());
                    }
                })
                .collectList()
                .map(filePaths -> new ProjectFilesDto(projectId, filePaths));
    }

    /**
     * 지정된 디렉토리 내의 파일 목록을 가져옵니다.
     */
    private Flux<String> retrieveFilesInDirectory(String projectId, String directoryPath) {
        return gitLabApiClient.getRepositoryFiles(projectId, directoryPath)
                .filter(file -> file.hasExtension(gitLabProperties.getFileExtension()))
                .map(GitLabFileDto::getPath);
    }

    /**
     * 각 프로젝트의 파일 내용을 가져옵니다.
     */
    public Mono<AllFilesContentDto> retrieveFileContents(List<ProjectFilesDto> projectFilesList) {

        List<Mono<FileContentDto>> fileContentMonos = new ArrayList<>();

        for (ProjectFilesDto projectFiles : projectFilesList) {
            String projectId = projectFiles.getProjectId();
            List<String> filePaths = projectFiles.getFilePaths();

            for (String filePath : filePaths) {
                Mono<FileContentDto> fileContentMono =
                        gitLabApiClient.getFileContent(projectId, filePath)
                                .map(content -> new FileContentDto(filePath, content))
                                .doOnSuccess(fileContent ->
                                        log.info("파일 내용 가져옴: {}", fileContent.getFilePath()));

                fileContentMonos.add(fileContentMono);
            }
        }

        return Flux.concat(fileContentMonos)
                .collectList()
                .map(AllFilesContentDto::new);
    }

    /**
     * 단일 파일 내용을 가져옵니다.
     */
    public Mono<String> retrieveSingleFileContent(String projectId, String filePath) {
        return gitLabApiClient.getFileContent(projectId, filePath);
    }

    /**
     * 프로젝트의 파일 목록을 가져옵니다.
     */
    public Flux<String> retrieveProjectFiles(String projectId) {
        return gitLabApiClient.getRepositoryFiles(projectId, "")
                .filter(file -> file.hasExtension(gitLabProperties.getFileExtension()) || file.isDirectory())
                .flatMap(file -> {
                    if (file.isDirectory()) {
                        return retrieveFilesInDirectory(projectId, file.getPath());
                    } else {
                        return Mono.just(file.getPath());
                    }
                });
    }

    /**
     * 파일 객체가 디렉토리인지 확인합니다.
     */
    private boolean isDirectory(String type) {
        return "tree".equals(type);
    }
}
