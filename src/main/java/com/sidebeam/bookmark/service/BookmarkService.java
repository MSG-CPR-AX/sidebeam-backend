package com.sidebeam.bookmark.service;

import com.sidebeam.bookmark.dto.BookmarkDto;
import com.sidebeam.bookmark.dto.CategoryNodeDto;

import java.util.List;


public interface BookmarkService {

    /**
     * 모든 북마크를 가져옵니다.
     * 저장된 모든 YAML 파일에서 추출한 북마크 목록을 반환합니다.
     */
    List<BookmarkDto> retrieveAllBookmarks();

    /**
     * 북마크 카테고리를 기반으로 한 트리 구조를 생성합니다.
     * 이 메서드는 북마크의 카테고리 정보를 사용하여 계층적인 카테고리 트리를 구성합니다.
     * 트리는 북마크 데이터를 탐색하고 카테고리별로 정렬하는 데 사용됩니다.
     */
    CategoryNodeDto retrieveCategoryTree();

    /**
     * 북마크 데이터를 최신 상태로 갱신합니다.
     * 외부 이벤트 (예: GitLab 웹훅) 또는 내부 트리거에 의해 호출될 수 있습니다.
     * 이 메서드는 북마크 서비스에 저장된 데이터의 일관성을 유지하는 데 사용됩니다.
     */
    void refreshBookmarks();
}