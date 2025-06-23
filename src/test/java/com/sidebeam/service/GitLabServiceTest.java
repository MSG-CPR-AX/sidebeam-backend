package com.sidebeam.service;

import com.sidebeam.external.gitlab.GitLabApiClient;
import com.sidebeam.bookmark.component.SpringCacheManager;
import com.sidebeam.external.gitlab.GitLabDataAggregator;
import com.sidebeam.external.gitlab.config.GitLabProperties;
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
    private GitLabDataAggregator gitLabDataAggregator;

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
                gitLabDataAggregator,
                springCacheManager
        );
    }

    @Test
    void fetchAllYamlFiles_shouldReturnCachedData_whenCacheHit() {
        // Arrange
        Map<String, String> cachedData = new HashMap<>();
        cachedData.put("file1.yml", "content1");
        cachedData.put("file2.yml", "content2");

        when(springCacheManager.getCachedData(Map.class)).thenReturn(Mono.just(cachedData));

        // Act
        Map<String, String> result = gitLabService.fetchAllYamlFiles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("content1", result.get("file1.yml"));
        assertEquals("content2", result.get("file2.yml"));

        verify(springCacheManager, times(1)).getCachedData(Map.class);
        verify(gitLabApiClient, never()).retrieveProjectIdListByGroupId(anyString());
    }

    @Test
    void fetchAllYamlFiles_shouldFetchFromGitLab_whenCacheMiss() {
        // Arrange
        when(springCacheManager.getCachedData(Map.class)).thenReturn(Mono.empty());
        when(gitLabProperties.getRootGroupId()).thenReturn("root-group-id");
        when(gitLabProperties.getFileExtension()).thenReturn(".yml");

        // Mock project data
        Map<String, Object> project = new HashMap<>();
        project.put("id", "123");
        project.put("path_with_namespace", "group/project");
        when(gitLabApiClient.retrieveProjectIdListByGroupId("root-group-id")).thenReturn(Flux.just(project));

        // Mock repository files
        Map<String, Object> file1 = new HashMap<>();
        file1.put("name", "file1.yml");
        file1.put("path", "file1.yml");
        file1.put("type", "blob");

        Map<String, Object> directory = new HashMap<>();
        directory.put("name", "dir");
        directory.put("path", "dir");
        directory.put("type", "tree");

        when(gitLabApiClient.getRepositoryFiles("123", "")).thenReturn(Flux.just(file1, directory));

        Map<String, Object> file2 = new HashMap<>();
        file2.put("name", "file2.yml");
        file2.put("path", "dir/file2.yml");
        file2.put("type", "blob");

        when(gitLabApiClient.getRepositoryFiles("123", "dir")).thenReturn(Flux.just(file2));

        // Mock file contents
        when(gitLabApiClient.getFileContentViaOpenUrl("123", "file1.yml"))
                .thenReturn(Mono.just("content1"));
        when(gitLabApiClient.getFileContentViaOpenUrl("123", "dir/file2.yml"))
                .thenReturn(Mono.just("content2"));

        // Mock cache
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("file1.yml", "content1");
        resultMap.put("dir/file2.yml", "content2");
        when(springCacheManager.cacheData(any(Map.class))).thenReturn(Mono.just(resultMap));

        // Act
        Map<String, String> result = gitLabService.fetchAllYamlFiles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("content1", result.get("file1.yml"));
        assertEquals("content2", result.get("dir/file2.yml"));

        verify(springCacheManager, times(1)).getCachedData(Map.class);
        verify(gitLabApiClient, times(1)).retrieveProjectIdListByGroupId("root-group-id");
        verify(gitLabApiClient, times(1)).getRepositoryFiles("123", "");
        verify(gitLabApiClient, times(1)).getRepositoryFiles("123", "dir");
        verify(gitLabApiClient, times(1)).getFileContentViaOpenUrl("123", "file1.yml");
        verify(gitLabApiClient, times(1)).getFileContentViaOpenUrl("123", "dir/file2.yml");
        verify(springCacheManager, times(1)).cacheData(any(Map.class));
    }

    @Test
    void fetchYamlFile_shouldReturnFileContent() {
        // Arrange
        String filePath = "file.yml";
        String projectId = "project-id";
        String content = "file content";

        when(gitLabProperties.getProjectId()).thenReturn(projectId);
        when(gitLabApiClient.getFileContentViaOpenUrl(projectId, filePath)).thenReturn(Mono.just(content));

        // Act
        String result = gitLabService.fetchYamlFile(filePath);

        // Assert
        assertEquals(content, result);
        verify(gitLabApiClient, times(1)).getFileContentViaOpenUrl(projectId, filePath);
    }

    @Test
    void listYamlFiles_shouldReturnFileList() {
        // Arrange
        String projectId = "project-id";
        when(gitLabProperties.getProjectId()).thenReturn(projectId);
        when(gitLabProperties.getFileExtension()).thenReturn(".yml");

        Map<String, Object> file1 = new HashMap<>();
        file1.put("name", "file1.yml");
        file1.put("path", "file1.yml");

        Map<String, Object> file2 = new HashMap<>();
        file2.put("name", "file2.txt");
        file2.put("path", "file2.txt");

        Map<String, Object> file3 = new HashMap<>();
        file3.put("name", "file3.yml");
        file3.put("path", "file3.yml");

        when(gitLabApiClient.getRepositoryFiles(projectId, "")).thenReturn(Flux.just(file1, file2, file3));

        // Act
        List<String> result = gitLabService.listYamlFiles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("file1.yml"));
        assertTrue(result.contains("file3.yml"));
        assertFalse(result.contains("file2.txt"));

        verify(gitLabApiClient, times(1)).getRepositoryFiles(projectId, "");
    }
}