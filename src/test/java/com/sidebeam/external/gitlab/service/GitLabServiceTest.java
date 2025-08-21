package com.sidebeam.external.gitlab.service;

import com.sidebeam.common.cache.component.SpringCacheManager;
import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.dto.GitLabProjectDto;
import com.sidebeam.external.gitlab.dto.AllFilesContentDto;
import com.sidebeam.external.gitlab.dto.FileContentDto;
import com.sidebeam.external.gitlab.dto.ProjectFilePathsDto;
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

import java.util.List;

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
        List<FileContentDto> fileContents = List.of(
            new FileContentDto("file1.yml", "content1"),
            new FileContentDto("file2.yml", "content2")
        );
        AllFilesContentDto cachedData = new AllFilesContentDto(fileContents);

        when(springCacheManager.getCachedData(AllFilesContentDto.class)).thenReturn(Mono.just(cachedData));

        // Act
        AllFilesContentDto result = gitLabService.retrieveAllYamlFiles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.fileContents().size());
        assertEquals("content1", result.fileContents().get(0).content());
        assertEquals("content2", result.fileContents().get(1).content());

        verify(springCacheManager, times(1)).getCachedData(AllFilesContentDto.class);
        verify(gitLabApiClient, never()).getProjectIdListByGroupId(anyString());
    }

    @Test
    void fetchAllYamlFiles_shouldRetrieveFromGitLab_whenCacheMiss() {
        // Arrange
        when(springCacheManager.getCachedData(AllFilesContentDto.class)).thenReturn(Mono.empty());
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
        ProjectFilePathsDto projectFiles = new ProjectFilePathsDto("123", List.of("file1.yml", "dir/file2.yml"));
        when(gitLabStorageFileRetriever.retrieveProjectFilePaths(project))
                .thenReturn(Mono.just(projectFiles));

        // Mock file contents
        List<FileContentDto> fileContents = List.of(
            new FileContentDto("file1.yml", "content1"),
            new FileContentDto("dir/file2.yml", "content2")
        );
        AllFilesContentDto resultDto = new AllFilesContentDto(fileContents);
        when(gitLabStorageFileRetriever.retrieveFileContents(List.of(projectFiles)))
                .thenReturn(Mono.just(resultDto));

        // Mock cache
        when(springCacheManager.cacheData(resultDto)).thenReturn(Mono.just(resultDto));

        // Act
        AllFilesContentDto result = gitLabService.retrieveAllYamlFiles();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.fileContents().size());
        assertEquals("content1", result.fileContents().get(0).content());
        assertEquals("content2", result.fileContents().get(1).content());

        verify(springCacheManager, times(1)).getCachedData(AllFilesContentDto.class);
        verify(gitLabApiClient, times(1)).getProjectIdListByGroupId("root-group-id");
        verify(gitLabStorageFileRetriever, times(1)).retrieveProjectFilePaths(project);
        verify(gitLabStorageFileRetriever, times(1)).retrieveFileContents(List.of(projectFiles));
        verify(springCacheManager, times(1)).cacheData(resultDto);
    }
}
