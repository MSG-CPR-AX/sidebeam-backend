package com.sidebeam.bookmark.mapper;

import com.sidebeam.bookmark.domain.model.Bookmark;
import com.sidebeam.bookmark.domain.model.CategoryNode;
import com.sidebeam.bookmark.domain.model.PackageNode;
import com.sidebeam.bookmark.dto.BookmarkDto;
import com.sidebeam.bookmark.dto.CategoryNodeDto;
import com.sidebeam.bookmark.dto.PackageNodeDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct mapper interface for converting between entity records and DTO records.
 */
@Mapper
public interface BookmarkMapper {

    BookmarkMapper INSTANCE = Mappers.getMapper(BookmarkMapper.class);

    /**
     * Converts a Bookmark entity to BookmarkDto.
     */
    BookmarkDto toDto(Bookmark bookmark);

    /**
     * Converts a list of Bookmark entities to BookmarkDto list.
     */
    List<BookmarkDto> toDto(List<Bookmark> bookmarks);

    /**
     * Converts a PackageNode entity to PackageNodeDto.
     */
    PackageNodeDto toDto(PackageNode packageNode);

    /**
     * Converts a CategoryNode entity to CategoryNodeDto.
     */
    CategoryNodeDto toDto(CategoryNode categoryNode);
}
