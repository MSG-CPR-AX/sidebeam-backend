package com.sidebar.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sidebar.config.CacheConfig;
import com.sidebar.model.Bookmark;
import com.sidebar.model.CategoryNode;
import com.sidebar.service.BookmarkService;
import com.sidebar.service.GitLabService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of BookmarkService.
 */
@Slf4j
@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final GitLabService gitLabService;
    private final ObjectMapper yamlMapper;

    public BookmarkServiceImpl(GitLabService gitLabService) {
        this.gitLabService = gitLabService;
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
    public List<Bookmark> getAllBookmarks() {
        log.info("Fetching all bookmarks from GitLab");
        List<Bookmark> bookmarks = new ArrayList<>();
        Map<String, String> yamlFiles = gitLabService.fetchAllYamlFiles();

        for (Map.Entry<String, String> entry : yamlFiles.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();
            try {
                List<Bookmark> fileBookmarks = parseYamlContent(content);
                // Set the source path for each bookmark
                fileBookmarks.forEach(bookmark -> bookmark.setSourcePath(filePath));
                bookmarks.addAll(fileBookmarks);
            } catch (Exception e) {
                log.error("Error parsing YAML file: {}", filePath, e);
            }
        }

        log.info("Fetched {} bookmarks from {} files", bookmarks.size(), yamlFiles.size());
        return bookmarks;
    }

    /**
     * 북마크 데이터를 기반으로 카테고리 트리를 생성하여 반환하는 메서드입니다.
     * 이 메서드는 캐시를 활용하여 성능을 최적화하며, 북마크 데이터를 가져와
     * 카테고리 리스트를 추출하고 이를 트리 구조로 빌드합니다.
     */
    @Override
    @Cacheable(CacheConfig.CATEGORY_TREE_CACHE)
    public CategoryNode getCategoryTree() {
        log.info("Building category tree");
        List<Bookmark> bookmarks = getAllBookmarks();
        List<String> categories = bookmarks.stream()
                .map(Bookmark::getCategory)
                .collect(Collectors.toList());

        CategoryNode root = CategoryNode.buildTree(categories);
        log.info("Built category tree with {} categories", categories.size());
        return root;
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
        log.info("Refreshing bookmark data");
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
        return yamlMapper.readValue(content,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, Bookmark.class));
    }

    /**
     * 애플리케이션 시작 시 북마크 데이터를 초기화하고 로드하는 메서드입니다.
     * 데이터를 로드하여 애플리케이션 내 북마크와 카테고리 트리를 준비합니다.
     * 북마크 데이터는 외부 소스에서 가져오며, 카테고리 구조는 이를 기반으로 빌드됩니다.
     * 애플리케이션 시작 후 바로 북마크 관리 기능이 원활히 작동하도록 보장합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadBookmarksOnStartup() {
        log.info("Loading bookmarks on startup");
        getAllBookmarks();
        getCategoryTree();
    }

    /**
     * 북마크 데이터를 주기적으로 새로고침하기 위해 스케줄링된 작업입니다.
     * 정해진 cron 표현식(매 시간 정각)에 따라 호출되며, 캐시된 북마크 데이터를 삭제하고
     * 향후 접근 시 최신 데이터를 가져올 수 있도록 준비합니다.
     *
     * 목적:
     * - 데이터의 최신 상태를 유지하기 위해 자동화된 새로고침을 제공합니다.
     * - 캐싱된 북마크와 카테고리 트리 데이터를 무효화하여 다음 요청 시 업데이트된 데이터가 사용되도록 보장합니다.
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void refreshBookmarksScheduled() {
        log.info("Scheduled refresh of bookmark data");
        refreshBookmarks();
    }
}