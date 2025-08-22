package com.sidebeam.bookmark.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sidebeam.bookmark.dto.BookmarkDto;
import com.sidebeam.bookmark.dto.CategoryNodeDto;
import com.sidebeam.bookmark.mapper.BookmarkMapper;
import com.sidebeam.common.cache.config.CacheConfig;
import com.sidebeam.bookmark.domain.model.Bookmark;
import com.sidebeam.bookmark.domain.model.CategoryNode;
import com.sidebeam.bookmark.domain.service.BookmarkValidator;
import com.sidebeam.bookmark.service.BookmarkService;
import com.sidebeam.bookmark.service.GitLabService;
import com.sidebeam.external.gitlab.dto.AllFilesContentDto;
import com.sidebeam.external.gitlab.dto.FileContentDto;
import com.sidebeam.bookmark.service.SchemaValidationService;
import com.sidebeam.bookmark.util.CategoryTreeBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of BookmarkService.
 */
@Slf4j
@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final GitLabService gitLabService;
    private final SchemaValidationService schemaValidationService;
    private final BookmarkValidator bookmarkValidator;
    private final ObjectMapper yamlMapper;

    /**
     * SonarQube S6809 이슈 해결을 위한 자기 자신 프록시 주입
     * 
     * Spring에서 @Cacheable과 같은 프록시 어노테이션이 적용된 메서드를 동일 클래스 내에서 호출할 때,
     * 'this'를 통한 직접 호출은 Spring 프록시를 우회하여 어노테이션 기능이 작동하지 않습니다.
     * 
     * 이 문제를 해결하기 위해 자기 자신(BookmarkService 인터페이스)을 @Lazy로 주입받아
     * Spring 프록시를 통해 메서드를 호출하도록 합니다.
     * 
     * @Lazy 어노테이션은 순환 참조 문제를 방지하기 위해 사용됩니다.
     */
    @Autowired @Lazy
    private BookmarkService self;

    public BookmarkServiceImpl(GitLabService gitLabService, SchemaValidationService schemaValidationService, BookmarkValidator bookmarkValidator) {
        this.gitLabService = gitLabService;
        this.schemaValidationService = schemaValidationService;
        this.bookmarkValidator = bookmarkValidator;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * GitLab에서 모든 북마크 데이터를 가져와 반환합니다.
     * 북마크 정보는 YAML 파일에서 파싱되며, 각 북마크는 해당 파일의 경로를 소스 경로로 설정합니다.
     * 캐시는 BOOKMARKS_CACHE 설정을 따르며, 캐시된 데이터를 사용할 경우 GitLab에 데이터를 요청하지 않습니다.
     * GitLab에서 가져온 YAML 파일을 처리하는 동안 발생할 수 있는 오류는 로그로 기록되지만,
     * 나머지 데이터 처리는 계속 진행됩니다.
     */
    @Override
    @Cacheable(CacheConfig.BOOKMARKS_CACHE)
    public List<BookmarkDto> retrieveAllBookmarks() {
        log.debug("Fetching all bookmarks from GitLab");

        // 모든 데이터 파일 조회
        AllFilesContentDto allFilesContent = gitLabService.retrieveAllYamlFiles();

        // 정의된 스키마 기반 조회된 모든 YAML의 유효성 검증
        schemaValidationService.validateAllYamlFiles(allFilesContent);
        log.debug("All YAML files passed schema validation");

        List<Bookmark> bookmarks = new ArrayList<>();

        for (FileContentDto fileContent : allFilesContent.fileContents()) {
            String filePath = fileContent.filePath();
            String content = fileContent.content();
            try {
                bookmarks.addAll(this.parseYamlContent(content));
            } catch (Exception e) {
                log.error("Error parsing YAML file: {}", filePath, e);
            }
        }

        // 중복 URL 존재 유무 검증
        bookmarkValidator.checkDuplicateUrls(bookmarks);

        log.debug("Fetched {} bookmarks from {} files", bookmarks.size(), allFilesContent.fileContents().size());
        return BookmarkMapper.INSTANCE.toDto(bookmarks);
    }

    /**
     * 북마크 데이터를 기반으로 카테고리 트리를 생성하여 반환하는 메서드입니다.
     * 이 메서드는 캐시를 활용하여 성능을 최적화하며, 북마크 데이터를 가져와
     * 카테고리 리스트를 추출하고 이를 트리 구조로 빌드합니다.
     * 
     * SonarQube S6809 이슈 해결: self 프록시를 통해 @Cacheable 메서드 호출
     */
    @Override
    @Cacheable(CacheConfig.CATEGORY_TREE_CACHE)
    public CategoryNodeDto retrieveCategoryTree() {
        log.debug("Building category tree");
        // self 프록시를 통해 @Cacheable 어노테이션이 적용된 메서드를 호출하여
        // Spring AOP 프록시 메커니즘이 정상적으로 작동하도록 보장합니다.
        List<BookmarkDto> bookmarks = self.retrieveAllBookmarks();
        List<String> categories = bookmarks.stream()
                .map(BookmarkDto::category)
                .toList();

        CategoryNode root = CategoryTreeBuilder.buildTree(categories);
        log.debug("Built category tree with {} categories", categories.size());
        return BookmarkMapper.INSTANCE.toDto(root);
    }

    /**
     * 북마크 및 카테고리 트리 데이터의 캐시를 갱신하는 메서드.
     *
     * 이 메서드는 기존 캐시된 데이터를 모두 제거하여 다음 요청 시
     * 데이터가 다시 로드되도록 강제합니다. 이를 통해 변경된 콘텐츠나
     * 최신 데이터가 반영되도록 보장합니다.
     * 이 작업은 애플리케이션의 데이터 일관성을 유지하기 위해 필요합니다.
     */
    @Override
    @CacheEvict(value = {CacheConfig.BOOKMARKS_CACHE, CacheConfig.CATEGORY_TREE_CACHE}, allEntries = true)
    public void refreshBookmarks() {
        log.debug("Refreshing bookmark data");
        // The cache eviction will force a reload on next access
    }

    /**
     * 주어진 YAML 콘텐츠를 파싱하여 Bookmark 객체의 리스트로 반환합니다.
     *
     * @param content YAML 형식의 문자열 콘텐츠
     * @return 파싱된 Bookmark 객체의 리스트
     * @throws IOException YAML 파싱 중 오류가 발생한 경우
     */
    private List<Bookmark> parseYamlContent(String content) throws IOException {
        return yamlMapper.readValue(content, yamlMapper.getTypeFactory().constructCollectionType(List.class, Bookmark.class));
    }
}
