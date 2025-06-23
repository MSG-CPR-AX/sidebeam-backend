package com.sidebeam.service;

import com.sidebeam.bookmark.domain.model.Bookmark;
import com.sidebeam.bookmark.domain.model.CategoryNode;
import com.sidebeam.bookmark.service.BookmarkService;
import com.sidebeam.bookmark.service.GitLabService;
import com.sidebeam.bookmark.service.SchemaValidationService;
import com.sidebeam.bookmark.service.impl.BookmarkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private GitLabService gitLabService;

    @Mock
    private SchemaValidationService schemaValidationService;

    private BookmarkService bookmarkService;

    private final String TEST_YAML = """
            # Test bookmarks for validation
            - name: GitLab Docs
              url: https://docs.gitlab.com
              domain: docs.gitlab.com
              category: DevOps/GitLab
              packages:
                - key: dev
                  children:
                    - key: doc
                      children:
                        - key: gitlab
              meta:
                priority: 1
                owner: devops-team

            # Valid bookmark with minimum required fields
            - name: Google
              url: https://www.google.com
              domain: www.google.com
              category: Search/Engine
            """;

    @BeforeEach
    void setUp() {
        bookmarkService = new BookmarkServiceImpl(gitLabService, schemaValidationService);
    }

    @Test
    void getAllBookmarks_shouldReturnBookmarks() {
        // Arrange
        Map<String, String> yamlFiles = new HashMap<>();
        // Use the new key format: "moduleName:fileName"
        yamlFiles.put("ops:test_bookmarks.yml", TEST_YAML);
        when(gitLabService.fetchAllYamlFiles()).thenReturn(yamlFiles);

        // Act
        List<Bookmark> bookmarks = bookmarkService.getAllBookmarks();

        // GitLabService 호출 결과 로깅
        log.info("===== GitLabService.fetchAllYamlFiles() 호출 결과 =====");
        log.info("YAML 파일 수: {}", yamlFiles.size());
        yamlFiles.forEach((path, content) -> {
            log.info("파일 경로: {}", path);
            log.info("파일 내용: \n{}", content);
        });

        // BookmarkService 호출 결과 로깅
        log.info("\n===== BookmarkService.getAllBookmarks() 호출 결과 =====");
        log.info("북마크 총 개수: {}", bookmarks.size());
        bookmarks.forEach(bookmark -> {
            log.info("-------------------------------------------");
            log.info("북마크 이름: {}", bookmark.getName());
            log.info("URL: {}", bookmark.getUrl());
            log.info("도메인: {}", bookmark.getDomain());
            log.info("카테고리: {}", bookmark.getCategory());
            log.info("메타데이터: {}", bookmark.getMeta());
            log.info("패키지: {}", bookmark.getPackages());
            log.info("소스 경로: {}", bookmark.getSourcePath());
        });

        // Assert
        assertNotNull(bookmarks);
        assertEquals(2, bookmarks.size());

        Bookmark bookmark1 = bookmarks.get(0);
        assertEquals("GitLab Docs", bookmark1.getName());
        assertEquals("https://docs.gitlab.com", bookmark1.getUrl());
        assertEquals("docs.gitlab.com", bookmark1.getDomain());
        assertEquals("DevOps/GitLab", bookmark1.getCategory());
        assertNotNull(bookmark1.getMeta());
        // Now expecting 3 items in metadata: priority, owner, and module
        assertEquals(3, bookmark1.getMeta().size());
        // Verify the module metadata was added correctly
        assertEquals("ops", bookmark1.getMeta().get("module"));
        assertEquals(1, bookmark1.getPackages().size());

        Bookmark bookmark2 = bookmarks.get(1);
        assertEquals("Google", bookmark2.getName());
        assertEquals("https://www.google.com", bookmark2.getUrl());
        assertEquals("www.google.com", bookmark2.getDomain());
        assertEquals("Search/Engine", bookmark2.getCategory());
        assertNotNull(bookmark2.getMeta());
        // Verify the module metadata was added correctly
        assertEquals(1, bookmark2.getMeta().size());
        assertEquals("ops", bookmark2.getMeta().get("module"));
        assertNull(bookmark2.getPackages());

        verify(gitLabService, times(1)).fetchAllYamlFiles();
    }

    @Test
    void getCategoryTree_shouldReturnCategoryTree() {
        // Arrange
        Map<String, String> yamlFiles = new HashMap<>();
        // Use the new key format: "moduleName:fileName"
        yamlFiles.put("ops:test_bookmarks.yml", TEST_YAML);
        when(gitLabService.fetchAllYamlFiles()).thenReturn(yamlFiles);

        // Act
        CategoryNode categoryTree = bookmarkService.getCategoryTree();

        // BookmarkService 호출 결과 로깅
        log.info("\n===== BookmarkService.getCategoryTree() 호출 결과 =====");
        log.info("루트 카테고리 이름: {}", categoryTree.getName());
        log.info("카테고리 수: {}", categoryTree.getChildren().size());

        categoryTree.getChildren().forEach(category -> {
            log.info("-------------------------------------------");
            log.info("카테고리 이름: {}", category.getName());
            log.info("하위 카테고리 수: {}", category.getChildren().size());

            category.getChildren().forEach(subCategory -> {
                log.info("  - 하위 카테고리: {}", subCategory.getName());
            });
        });

        // Assert
        assertNotNull(categoryTree);
        assertEquals("root", categoryTree.getName());
        assertEquals(2, categoryTree.getChildren().size());

        // Find DevOps category
        CategoryNode devOpsNode = categoryTree.getChildren().stream()
                .filter(node -> "DevOps".equals(node.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(devOpsNode);
        assertEquals(1, devOpsNode.getChildren().size());

        // Find Search category
        CategoryNode searchNode = categoryTree.getChildren().stream()
                .filter(node -> "Search".equals(node.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(searchNode);
        assertEquals(1, searchNode.getChildren().size());

        verify(gitLabService, times(1)).fetchAllYamlFiles();
    }

    @Test
    void refreshBookmarks_shouldClearCache() {
        // Act
        bookmarkService.refreshBookmarks();

        // BookmarkService 호출 결과 로깅
        log.info("\n===== BookmarkService.refreshBookmarks() 호출 결과 =====");
        log.info("캐시를 성공적으로 비웠습니다.");

        // No assertions needed as this just evicts the cache
        // The method doesn't return anything or have observable side effects
    }

    @Test
    void getAllBookmarks_shouldDetectDuplicateUrls() {
        // Arrange
        String yamlWithDuplicateUrls = """
                # First bookmark
                - name: GitLab Docs
                  url: https://docs.gitlab.com
                  domain: docs.gitlab.com
                  category: DevOps/GitLab

                # Duplicate URL
                - name: GitLab Documentation
                  url: https://docs.gitlab.com
                  domain: docs.gitlab.com
                  category: Documentation/GitLab
                """;

        Map<String, String> yamlFiles = new HashMap<>();
        // Use the new key format: "moduleName:fileName"
        yamlFiles.put("ops:test_bookmarks.yml", yamlWithDuplicateUrls);
        when(gitLabService.fetchAllYamlFiles()).thenReturn(yamlFiles);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bookmarkService.getAllBookmarks();
        });

        // Verify the exception message contains information about duplicate URLs
        assertTrue(exception.getMessage().contains("Duplicate URLs found"));

        // Verify that GitLabService was called
        verify(gitLabService, times(1)).fetchAllYamlFiles();

        log.info("\n===== Duplicate URL Detection Test =====");
        log.info("Exception message: {}", exception.getMessage());
    }
}
