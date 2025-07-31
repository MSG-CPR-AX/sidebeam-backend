package com.sidebeam.bookmark.service.impl;

import com.sidebeam.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 북마크 초기화 및 스케줄링 전담 서비스
 * 
 * SonarQube java:S6809 이슈 해결을 위해 BookmarkServiceImpl에서 분리된 컴포넌트입니다.
 * Spring 프록시 어노테이션(@Cacheable 등)이 적용된 메서드들을 올바르게 호출하기 위해
 * BookmarkService 인터페이스를 통해 프록시를 거쳐 메서드를 호출합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkInitializationService {

    private final BookmarkService bookmarkService;

    /**
     * 애플리케이션 시작 시 북마크 데이터를 초기화하고 로드하는 메서드입니다.
     * 데이터를 로드하여 애플리케이션 내 북마크와 카테고리 트리를 준비합니다.
     * 북마크 데이터는 외부 소스에서 가져오며, 카테고리 구조는 이를 기반으로 빌드됩니다.
     * 애플리케이션 시작 후 바로 북마크 관리 기능이 원활히 작동하도록 보장합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadBookmarksOnStartup() {
        log.info("Loading bookmarks on startup");
        bookmarkService.retrieveAllBookmarks();
        bookmarkService.retrieveCategoryTree();
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
        bookmarkService.refreshBookmarks();
    }
}