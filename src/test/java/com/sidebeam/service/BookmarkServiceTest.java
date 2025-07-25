package com.sidebeam.service;

import com.sidebeam.bookmark.dto.BookmarkDto;
import com.sidebeam.bookmark.dto.CategoryNodeDto;
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
        BookmarkServiceImpl impl = new BookmarkServiceImpl(gitLabService, schemaValidationService);

        // 단위 테스트 환경에서 self-injection 필드를 수동으로 설정
        // Spring 컨테이너가 없는 환경에서 @Autowired가 작동하지 않으므로 리플렉션을 사용
        try {
            java.lang.reflect.Field selfField = BookmarkServiceImpl.class.getDeclaredField("self");
            selfField.setAccessible(true);
            selfField.set(impl, impl); // 자기 자신을 self 필드에 설정
        } catch (Exception e) {
            throw new RuntimeException("테스트를 위한 self 필드 설정 실패", e);
        }

        bookmarkService = impl;
    }

    @Test
    void retrieveAllBookmarks_shouldReturnBookmarks() {
        // Arrange
        Map<String, String> yamlFiles = new HashMap<>();
        // Use the new key format: "moduleName:fileName"
        yamlFiles.put("ops:test_bookmarks.yml", TEST_YAML);
        when(gitLabService.retrieveAllYamlFiles()).thenReturn(yamlFiles);

        // Act
        List<BookmarkDto> bookmarks = bookmarkService.retrieveAllBookmarks();

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
            log.info("북마크 이름: {}", bookmark.name());
            log.info("URL: {}", bookmark.url());
            log.info("도메인: {}", bookmark.domain());
            log.info("카테고리: {}", bookmark.category());
            log.info("메타데이터: {}", bookmark.meta());
            log.info("패키지: {}", bookmark.packages());
            log.info("소스 경로: {}", bookmark.sourcePath());
        });

        // Assert
        assertNotNull(bookmarks);
        assertEquals(2, bookmarks.size());

        BookmarkDto bookmark1 = bookmarks.get(0);
        assertEquals("GitLab Docs", bookmark1.name());
        assertEquals("https://docs.gitlab.com", bookmark1.url());
        assertEquals("docs.gitlab.com", bookmark1.domain());
        assertEquals("DevOps/GitLab", bookmark1.category());
        assertNotNull(bookmark1.meta());
        // Now expecting 3 items in metadata: priority, owner, and module
        assertEquals(3, bookmark1.meta().size());
        // Verify the module metadata was added correctly
        assertEquals("ops", bookmark1.meta().get("module"));
        assertEquals(1, bookmark1.packages().size());

        BookmarkDto bookmark2 = bookmarks.get(1);
        assertEquals("Google", bookmark2.name());
        assertEquals("https://www.google.com", bookmark2.url());
        assertEquals("www.google.com", bookmark2.domain());
        assertEquals("Search/Engine", bookmark2.category());
        assertNotNull(bookmark2.meta());
        // Verify the module metadata was added correctly
        assertEquals(1, bookmark2.meta().size());
        assertEquals("ops", bookmark2.meta().get("module"));
        assertNull(bookmark2.packages());

        verify(gitLabService, times(1)).retrieveAllYamlFiles();
    }

    @Test
    void getCategoryTree_shouldReturnCategoryTree() {
        // Arrange
        Map<String, String> yamlFiles = new HashMap<>();
        // Use the new key format: "moduleName:fileName"
        yamlFiles.put("ops:test_bookmarks.yml", TEST_YAML);
        when(gitLabService.retrieveAllYamlFiles()).thenReturn(yamlFiles);

        // Act
        CategoryNodeDto categoryTree = bookmarkService.getCategoryTree();

        // BookmarkService 호출 결과 로깅
        log.info("\n===== BookmarkService.getCategoryTree() 호출 결과 =====");
        log.info("루트 카테고리 이름: {}", categoryTree.name());
        log.info("카테고리 수: {}", categoryTree.children().size());

        categoryTree.children().forEach(category -> {
            log.info("-------------------------------------------");
            log.info("카테고리 이름: {}", category.name());
            log.info("하위 카테고리 수: {}", category.children().size());

            category.children().forEach(subCategory -> {
                log.info("  - 하위 카테고리: {}", subCategory.name());
            });
        });

        // Assert
        assertNotNull(categoryTree);
        assertEquals("root", categoryTree.name());
        assertEquals(2, categoryTree.children().size());

        // Find DevOps category
        CategoryNodeDto devOpsNode = categoryTree.children().stream()
                .filter(node -> "DevOps".equals(node.name()))
                .findFirst()
                .orElse(null);
        assertNotNull(devOpsNode);
        assertEquals(1, devOpsNode.children().size());

        // Find Search category
        CategoryNodeDto searchNode = categoryTree.children().stream()
                .filter(node -> "Search".equals(node.name()))
                .findFirst()
                .orElse(null);
        assertNotNull(searchNode);
        assertEquals(1, searchNode.children().size());

        verify(gitLabService, times(1)).retrieveAllYamlFiles();
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
    void retrieveAllBookmarks_shouldDetectDuplicateUrls() {
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
        when(gitLabService.retrieveAllYamlFiles()).thenReturn(yamlFiles);

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bookmarkService.retrieveAllBookmarks();
        });

        // Verify the exception message contains information about duplicate URLs
        assertTrue(exception.getMessage().contains("Duplicate URLs found"));

        // Verify that GitLabService was called
        verify(gitLabService, times(1)).retrieveAllYamlFiles();

        log.info("\n===== Duplicate URL Detection Test =====");
        log.info("Exception message: {}", exception.getMessage());
    }
}
