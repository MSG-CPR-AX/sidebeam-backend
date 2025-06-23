package com.sidebeam.bookmark.repository;

import com.sidebeam.bookmark.domain.model.Bookmark;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookmarkRepositoryImpl implements BookmarkRepository {


    @Override
    public List<Bookmark> retrieveAllBookmarks() {


        return null;
    }
}
