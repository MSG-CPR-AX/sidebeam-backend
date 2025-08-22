package com.sidebeam.bookmark.service;

import com.sidebeam.bookmark.service.impl.GitLabServiceImpl;
import com.sidebeam.common.cache.component.SpringCacheManager;
import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.dto.AllFilesContentDto;
import com.sidebeam.external.gitlab.dto.FileContentDto;
import com.sidebeam.external.gitlab.dto.ProjectFilePathsDto;
import com.sidebeam.external.gitlab.service.GitLabApiClient;
import com.sidebeam.external.gitlab.service.GitLabStorageFileRetriever;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitLabServiceImplTest {

    @Mock
    GitLabProperties gitLabProperties;
    @Mock
    GitLabApiClient gitLabApiClient;
    @Mock
    GitLabStorageFileRetriever fileRetriever;
    @Mock
    SpringCacheManager springCacheManager;

    @InjectMocks
    GitLabServiceImpl sut;

    @Test
    @DisplayName("Given empty rootGroupId when retrieve YAML files then returns empty result and skips API calls")
    void givenEmptyRootGroupId_whenRetrieveYamlFiles_thenReturnsEmptyAndSkipsApiCalls() {
        // Given
        given(springCacheManager.getCachedData(AllFilesContentDto.class)).willReturn(Mono.empty());
        given(gitLabProperties.getRootGroupId()).willReturn("");

        // When
        AllFilesContentDto result = sut.retrieveAllYamlFiles();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.fileContents()).isEmpty();
        verifyNoInteractions(gitLabApiClient, fileRetriever);
    }

    @Test
    @DisplayName("Given null rootGroupId when retrieve YAML files then returns empty result and skips API calls")
    void givenNullRootGroupId_whenRetrieveYamlFiles_thenReturnsEmptyAndSkipsApiCalls() {
        // Given
        given(springCacheManager.getCachedData(AllFilesContentDto.class)).willReturn(Mono.empty());
        given(gitLabProperties.getRootGroupId()).willReturn(null);

        // When
        AllFilesContentDto result = sut.retrieveAllYamlFiles();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.fileContents()).isEmpty();
        verifyNoInteractions(gitLabApiClient, fileRetriever);
    }

    @Test
    @DisplayName("Given cached data when retrieve YAML files then returns from cache and skips API calls")
    void givenCachedData_whenRetrieveYamlFiles_thenReturnsFromCacheAndSkipsApiCalls() {
        // Given
        AllFilesContentDto cached = new AllFilesContentDto(List.of(new FileContentDto("p.yml", "content")));
        given(springCacheManager.getCachedData(AllFilesContentDto.class)).willReturn(Mono.just(cached));

        // When
        AllFilesContentDto result = sut.retrieveAllYamlFiles();

        // Then
        assertThat(result).isSameAs(cached);
        verifyNoInteractions(gitLabApiClient, fileRetriever);
    }

    @Test
    @DisplayName("Given no cache and valid rootGroupId when retrieve YAML files then aggregates and caches data")
    void givenNoCacheAndValidRootGroupId_whenRetrieveYamlFiles_thenAggregatesAndCachesData() {
        // Given
        given(springCacheManager.getCachedData(AllFilesContentDto.class)).willReturn(Mono.empty());
        given(gitLabProperties.getRootGroupId()).willReturn("123");

        com.sidebeam.external.gitlab.dto.GitLabProjectDto project = new com.sidebeam.external.gitlab.dto.GitLabProjectDto(
                1L, null, null, null, null, null, null, null,
                null, null, "test-project", "test namespace", "test-path", "ns/test-path",
                null, null, null, null
        );
        given(gitLabApiClient.getProjectIdListByGroupId("123")).willReturn(Flux.just(project));

        ProjectFilePathsDto projectFiles = new ProjectFilePathsDto("1", List.of("bookmarks.yml"));
        given(fileRetriever.retrieveProjectFilePaths(any())).willReturn(Mono.just(projectFiles));

        AllFilesContentDto aggregated = new AllFilesContentDto(List.of(new FileContentDto("bookmarks.yml", "name: Test\nurl: https://example.com")));
        given(fileRetriever.retrieveFileContents(any())).willReturn(Mono.just(aggregated));
        given(springCacheManager.cacheData(aggregated)).willReturn(Mono.just(aggregated));

        // When
        AllFilesContentDto result = sut.retrieveAllYamlFiles();

        // Then
        assertThat(result.fileContents()).hasSize(1);
        assertThat(result.fileContents().get(0).filePath()).isEqualTo("bookmarks.yml");
        verify(gitLabApiClient).getProjectIdListByGroupId("123");
        verify(fileRetriever).retrieveProjectFilePaths(any());
        verify(fileRetriever).retrieveFileContents(any());
        verify(springCacheManager).cacheData(aggregated);
    }

    @Test
    @DisplayName("Given multiple projects when retrieve YAML files then processes all projects")
    void givenMultipleProjects_whenRetrieveYamlFiles_thenProcessesAllProjects() {
        // Given
        given(springCacheManager.getCachedData(AllFilesContentDto.class)).willReturn(Mono.empty());
        given(gitLabProperties.getRootGroupId()).willReturn("123");

        com.sidebeam.external.gitlab.dto.GitLabProjectDto project1 = new com.sidebeam.external.gitlab.dto.GitLabProjectDto(
                1L, null, null, null, null, null, null, null,
                null, null, "project1", "ns1", "path1", "ns1/path1",
                null, null, null, null
        );
        com.sidebeam.external.gitlab.dto.GitLabProjectDto project2 = new com.sidebeam.external.gitlab.dto.GitLabProjectDto(
                2L, null, null, null, null, null, null, null,
                null, null, "project2", "ns2", "path2", "ns2/path2",
                null, null, null, null
        );
        given(gitLabApiClient.getProjectIdListByGroupId("123")).willReturn(Flux.just(project1, project2));

        ProjectFilePathsDto projectFiles1 = new ProjectFilePathsDto("1", List.of("file1.yml"));
        ProjectFilePathsDto projectFiles2 = new ProjectFilePathsDto("2", List.of("file2.yml"));
        given(fileRetriever.retrieveProjectFilePaths(project1)).willReturn(Mono.just(projectFiles1));
        given(fileRetriever.retrieveProjectFilePaths(project2)).willReturn(Mono.just(projectFiles2));

        AllFilesContentDto aggregated = new AllFilesContentDto(List.of(
                new FileContentDto("file1.yml", "content1"),
                new FileContentDto("file2.yml", "content2")
        ));
        given(fileRetriever.retrieveFileContents(any())).willReturn(Mono.just(aggregated));
        given(springCacheManager.cacheData(aggregated)).willReturn(Mono.just(aggregated));

        // When
        AllFilesContentDto result = sut.retrieveAllYamlFiles();

        // Then
        assertThat(result.fileContents()).hasSize(2);
        verify(gitLabApiClient).getProjectIdListByGroupId("123");
        verify(fileRetriever).retrieveProjectFilePaths(project1);
        verify(fileRetriever).retrieveProjectFilePaths(project2);
        verify(fileRetriever).retrieveFileContents(any());
        verify(springCacheManager).cacheData(aggregated);
    }
}
