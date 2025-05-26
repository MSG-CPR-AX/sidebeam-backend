package com.sidebar.controller;

import com.sidebar.model.Bookmark;
import com.sidebar.model.CategoryNode;
import com.sidebar.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 북마크 데이터를 제공하는 API 컨트롤러입니다.
 * 북마크 목록 조회 및 카테고리 트리 구조를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/bookmarks")
@Tag(name = "Bookmarks", description = "API for accessing bookmark data")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    /**
     * 모든 북마크를 가져옵니다.
     * YAML 파일에서 가져온 모든 북마크 목록을 반환합니다.
     */
    @GetMapping
    @Operation(summary = "Get all bookmarks", description = "Returns a list of all bookmarks from all YAML files")
    public ResponseEntity<List<Bookmark>> getAllBookmarks() {
        log.info("REST request to get all bookmarks");
        return ResponseEntity.ok(bookmarkService.getAllBookmarks());
    }

    /**
     * 북마크 카테고리 트리 구조를 반환합니다.
     * 이 메서드는 모든 북마크의 카테고리를 기반으로 계층적 트리 구조를 생성합니다.
     * 트리는 루트 노드에서 시작하여 하위 카테고리로 확장됩니다.
     */
    @GetMapping("/categories")
    @Operation(summary = "Get category tree", description = "Returns a hierarchical tree of all bookmark categories")
    public ResponseEntity<CategoryNode> getCategoryTree() {
        log.info("REST request to get category tree");
        return ResponseEntity.ok(bookmarkService.getCategoryTree());
    }
}