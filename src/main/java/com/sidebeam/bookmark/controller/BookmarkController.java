package com.sidebeam.bookmark.controller;

import com.sidebeam.bookmark.dto.BookmarkDto;
import com.sidebeam.bookmark.dto.CategoryNodeDto;
import com.sidebeam.bookmark.mapper.BookmarkMapper;
import com.sidebeam.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 북마크 데이터를 제공하는 API 컨트롤러입니다.
 * 북마크 목록 조회 및 카테고리 트리 구조를 제공합니다.
 * 이제 DTO 클래스를 반환하여 프레젠테이션 레이어와 도메인 모델을 분리합니다.
 */
@Slf4j
@Tag(name = "Bookmarks", description = "API for accessing bookmark data")
@RequestMapping("/bookmarks")
@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 모든 북마크를 가져옵니다.
     * YAML 파일에서 가져온 모든 북마크 목록을 DTO로 변환하여 반환합니다.
     * GlobalResponseBodyAdvice에 의해 자동으로 ApiResponse로 래핑됩니다.
     */
    @GetMapping
    @Operation(summary = "Get all bookmarks", description = "Returns a list of all bookmarks from all YAML files")
    public List<BookmarkDto> retrieveAllBookmarks() {
        log.info("REST request to get all bookmarks");
        return BookmarkMapper.toDto(bookmarkService.retrieveAllBookmarks());
    }

    /**
     * 북마크 카테고리 트리 구조를 반환합니다.
     * 이 메서드는 모든 북마크의 카테고리를 기반으로 계층적 트리 구조를 생성하고
     * DTO로 변환하여 반환합니다.
     * 트리는 루트 노드에서 시작하여 하위 카테고리로 확장됩니다.
     * GlobalResponseBodyAdvice에 의해 자동으로 ApiResponse로 래핑됩니다.
     */
    @GetMapping("/categories")
    @Operation(summary = "Get category tree", description = "Returns a hierarchical tree of all bookmark categories")
    public CategoryNodeDto getCategoryTree() {
        log.info("REST request to get category tree");
        return BookmarkMapper.toDto(bookmarkService.getCategoryTree());
    }
}
