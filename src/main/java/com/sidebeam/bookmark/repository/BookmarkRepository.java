package com.sidebeam.bookmark.repository;

import com.sidebeam.bookmark.domain.model.Bookmark;

import java.util.List;

public interface BookmarkRepository {

    List<Bookmark> retrieveAllBookmarks();


}
