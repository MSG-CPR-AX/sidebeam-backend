package com.sidebeam.service;

import com.sidebeam.external.gitlab.GitLabApiClient;
import com.sidebeam.bookmark.component.SpringCacheManager;
import com.sidebeam.external.gitlab.GitLabStorageFileRetriever;
import com.sidebeam.external.gitlab.config.GitLabProperties;
import com.sidebeam.external.gitlab.dto.GitLabProjectDto;
import com.sidebeam.bookmark.service.GitLabService;
import com.sidebeam.bookmark.service.impl.GitLabServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GitLabServiceTest {

    @Mock
    private GitLabApiClient gitLabApiClient;

    @Mock
    private GitLabStorageFileRetriever gitLabStorageFileRetriever;

    @Mock
    private SpringCacheManager springCacheManager;

    @Mock
    private GitLabProperties gitLabProperties;

    private GitLabService gitLabService;

    @BeforeEach
    void setUp() {
        gitLabService = new GitLabServiceImpl(
                gitLabProperties,
                gitLabApiClient,
                gitLabStorageFileRetriever,
                springCacheManager
        );
    }

    @Test
    void retrieveAllYamlFiles_shouldReturnCachedData_whenCacheHit() {
        // Arrange
        Map<String, String> cachedData = new HashMap<>();
        cachedData.put("file1.yml", "content1");
        cachedData.put("file2.yml", "content2");

        when(springCacheManager.getCachedData(Map.class)).thenReturn(Mono.just(cachedData));

        // Act
        Map<String, String> result = gitLabService.retrieveAllYamlFiles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("content1", result.get("file1.yml"));
        assertEquals("content2", result.get("file2.yml"));

        verify(springCacheManager, times(1)).getCachedData(Map.class);
        verify(gitLabApiClient, never()).getProjectIdListByGroupId(anyString());
    }

    @Test
    void fetchAllYamlFiles_shouldRetrieveFromGitLab_whenCacheMiss() {
        // Arrange
        when(springCacheManager.getCachedData(Map.class)).thenReturn(Mono.empty());
        when(gitLabProperties.getRootGroupId()).thenReturn("root-group-id");

        // Mock project data
        GitLabProjectDto project = new GitLabProjectDto(
                123L, "description", "main", "private", 
                "ssh://git@gitlab.com/group/project.git", 
                "https://gitlab.com/group/project.git",
                "https://gitlab.com/group/project", 
                "https://gitlab.com/group/project/-/blob/main/README.md",
                List.of(), null, "project", "group/project", 
                "project", "group/project", null, null, null, null
        );
        when(gitLabApiClient.getProjectIdListByGroupId("root-group-id")).thenReturn(Flux.just(project));

        // Mock file retriever methods
        Map<String, List<String>> projectFiles = new HashMap<>();
        projectFiles.put("123", List.of("file1.yml", "dir/file2.yml"));
        when(gitLabStorageFileRetriever.retrieverProjectFiles(project))
                .thenReturn(Mono.just(projectFiles));

        when(gitLabStorageFileRetriever.mergeProjectFiles(List.of(projectFiles)))
                .thenReturn(projectFiles);

        // Mock file contents
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("file1.yml", "content1");
        resultMap.put("dir/file2.yml", "content2");
        when(gitLabStorageFileRetriever.retrieveFileContents(projectFiles))
                .thenReturn(Mono.just(resultMap));

        // Mock cache
        when(springCacheManager.cacheData(resultMap)).thenReturn(Mono.just(resultMap));

        // Act
        Map<String, String> result = gitLabService.retrieveAllYamlFiles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("content1", result.get("file1.yml"));
        assertEquals("content2", result.get("dir/file2.yml"));

        verify(springCacheManager, times(1)).getCachedData(Map.class);
        verify(gitLabApiClient, times(1)).getProjectIdListByGroupId("root-group-id");
        verify(gitLabStorageFileRetriever, times(1)).retrieverProjectFiles(project);
        verify(gitLabStorageFileRetriever, times(1)).mergeProjectFiles(any());
        verify(gitLabStorageFileRetriever, times(1)).retrieveFileContents(projectFiles);
        verify(springCacheManager, times(1)).cacheData(resultMap);
    }

    @Test
    void fetchYamlFile_shouldReturnFileContent() {
        // Arrange
        String filePath = "file.yml";
        String projectId = "project-id";
        String content = "file content";

        when(gitLabProperties.getProjectId()).thenReturn(projectId);
        when(gitLabStorageFileRetriever.retrieveSingleFileContent(projectId, filePath)).thenReturn(Mono.just(content));

        // Act
        String result = gitLabService.retrieveYamlFile(filePath);

        // Assert
        assertEquals(content, result);
        verify(gitLabStorageFileRetriever, times(1)).retrieveSingleFileContent(projectId, filePath);
    }

    @Test
    void listYamlFiles_shouldReturnFileRetrieve() {
        // Arrange
        String projectId = "project-id";
        when(gitLabProperties.getProjectId()).thenReturn(projectId);

        List<String> expectedFiles = List.of("file1.yml", "file3.yml");
        when(gitLabStorageFileRetriever.retrieveProjectFiles(projectId))
                .thenReturn(Flux.fromIterable(expectedFiles));

        // Act
        List<String> result = gitLabService.retrieveYamlFiles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("file1.yml"));
        assertTrue(result.contains("file3.yml"));

        verify(gitLabStorageFileRetriever, times(1)).retrieveProjectFiles(projectId);
    }
}
