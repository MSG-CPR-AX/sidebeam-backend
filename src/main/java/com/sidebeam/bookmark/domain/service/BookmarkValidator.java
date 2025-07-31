package com.sidebeam.bookmark.domain.service;

import com.sidebeam.bookmark.domain.model.Bookmark;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 북마크 데이터의 무결성과 일관성을 검증하는 도메인 서비스입니다.
 * 북마크 목록에서 중복된 URL을 탐지하고 데이터 품질을 보장하는 역할을 담당합니다.
 */
@Slf4j
@Component
public class BookmarkValidator {

    /**
     * 모든 북마크에서 중복된 URL을 검사하고 발견 시 오류를 로그에 기록합니다.
     * 이는 데이터 무결성을 보장하기 위한 검증 단계입니다.
     */
    public void checkDuplicateUrls(List<Bookmark> bookmarks) {
        Map<String, List<Bookmark>> urlMap = new HashMap<>();

        // URL별로 북마크 그룹화
        for (Bookmark bookmark : bookmarks) {
            String url = bookmark.getUrl();
            if (!urlMap.containsKey(url)) {
                urlMap.put(url, new ArrayList<>());
            }
            urlMap.get(url).add(bookmark);
        }

        // 중복 검사
        boolean hasDuplicates = false;
        StringBuilder errorMessage = new StringBuilder("중복된 URL이 발견되었습니다:\n");

        for (Map.Entry<String, List<Bookmark>> entry : urlMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                hasDuplicates = true;
                errorMessage.append("URL: ").append(entry.getKey()).append("\n");
                for (Bookmark bookmark : entry.getValue()) {
                    errorMessage.append("  - ").append(bookmark.getName())
                            .append(" (").append(bookmark.getSourcePath()).append(")\n");
                }
            }
        }

        if (hasDuplicates) {
            log.error(errorMessage.toString());
            throw new IllegalStateException("북마크에서 중복된 URL이 발견되었습니다. 자세한 내용은 로그를 확인하세요.");
        }
    }
}