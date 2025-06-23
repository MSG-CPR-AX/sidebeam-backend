
package com.sidebeam.external.gitlab;

import com.sidebeam.external.gitlab.config.GitLabProperties;
import com.sidebeam.external.gitlab.dto.GitLabProjectDto;
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
    public Mono<Map<String, List<String>>> getProjectFiles(GitLabProjectDto project) {
        String projectId = project.id().toString();
        String projectPath = project.pathWithNamespace();
        log.info("프로젝트 {}의 파일 목록 가져오기", projectPath);

        return gitLabApiClient.getRepositoryFiles(projectId, "")
                .filter(file -> {
                    String name = (String) file.get("name");
                    return name.endsWith(gitLabProperties.getFileExtension()) ||
                            isDirectory(file);
                })
                .flatMap(file -> {
                    if (isDirectory(file)) {
                        String path = (String) file.get("path");
                        return getFilesInDirectory(projectId, path);
                    } else {
                        return Flux.just((String) file.get("path"));
                    }
                })
                .collectList()
                .map(filePaths -> {
                    Map<String, List<String>> result = new HashMap<>();
                    result.put(projectId, filePaths);
                    return result;
                });
    }

    /**
     * 지정된 디렉토리 내의 파일 목록을 가져옵니다.
     */
    private Flux<String> getFilesInDirectory(String projectId, String directoryPath) {
        return gitLabApiClient.getRepositoryFiles(projectId, directoryPath)
                .filter(subFile -> {
                    String name = (String) subFile.get("name");
                    return name.endsWith(gitLabProperties.getFileExtension());
                })
                .map(subFile -> (String) subFile.get("path"));
    }

    /**
     * 각 프로젝트의 파일 내용을 가져옵니다.
     */
    public Mono<Map<String, String>> fetchFileContents(Map<String, List<String>> projectFiles) {
        Map<String, String> result = new ConcurrentHashMap<>();
        List<Mono<Map.Entry<String, String>>> fileContentMonos = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : projectFiles.entrySet()) {
            String projectId = entry.getKey();
            List<String> filePaths = entry.getValue();

            for (String filePath : filePaths) {
                Mono<Map.Entry<String, String>> fileContentMono =
                        gitLabApiClient.getFileContentViaOpenUrl(projectId, filePath)
                                .map(content -> Map.entry(filePath, content))
                                .doOnSuccess(fileEntry ->
                                        log.info("파일 내용 가져옴: {}", fileEntry.getKey()));

                fileContentMonos.add(fileContentMono);
            }
        }

        return Flux.concat(fileContentMonos)
                .collectList()
                .map(entries -> {
                    for (Map.Entry<String, String> entry : entries) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                    return result;
                });
    }

    /**
     * 단일 파일 내용을 가져옵니다.
     */
    public Mono<String> fetchSingleFileContent(String projectId, String filePath) {
        return gitLabApiClient.getFileContentViaOpenUrl(projectId, filePath);
    }

    /**
     * 프로젝트의 파일 목록을 가져옵니다.
     */
    public Flux<String> listProjectFiles(String projectId) {
        return gitLabApiClient.getRepositoryFiles(projectId, "")
                .filter(file -> {
                    String name = (String) file.get("name");
                    return name.endsWith(gitLabProperties.getFileExtension());
                })
                .map(file -> (String) file.get("path"));
    }

    /**
     * 파일 객체가 디렉토리인지 확인합니다.
     */
    private boolean isDirectory(Map file) {
        return "tree".equals(file.get("type"));
    }

    /**
     * 여러 프로젝트의 파일 목록을 하나의 맵으로 병합합니다.
     */
    public Map<String, List<String>> mergeProjectFiles(List<Map<String, List<String>>> projectFilesList) {
        Map<String, List<String>> result = new HashMap<>();
        for (Map<String, List<String>> projectFiles : projectFilesList) {
            result.putAll(projectFiles);
        }
        return result;
    }
}
