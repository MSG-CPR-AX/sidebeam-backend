package com.sidebeam.bookmark.mapper;

import com.sidebeam.bookmark.domain.model.Bookmark;
import com.sidebeam.bookmark.domain.model.CategoryNode;
import com.sidebeam.bookmark.domain.model.PackageNode;
import com.sidebeam.bookmark.dto.BookmarkDto;
import com.sidebeam.bookmark.dto.CategoryNodeDto;
import com.sidebeam.bookmark.dto.PackageNodeDto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between entity records and DTO records.
 */
public class BookmarkMapper {

    private BookmarkMapper() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a Bookmark entity to BookmarkDto.
     */
    public static BookmarkDto toDto(Bookmark bookmark) {
        if (bookmark == null) {
            return null;
        }

        List<PackageNodeDto> packageDtos = bookmark.getPackages() != null 
            ? bookmark.getPackages().stream()
                .map(BookmarkMapper::toDto)
                .collect(Collectors.toList())
            : null;

        return new BookmarkDto(
            bookmark.getName(),
            bookmark.getUrl(),
            bookmark.getDomain(),
            bookmark.getCategory(),
            packageDtos,
            bookmark.getMeta(),
            bookmark.getSourcePath()
        );
    }

    /**
     * Converts a list of Bookmark entities to BookmarkDto list.
     */
    public static List<BookmarkDto> toDto(List<Bookmark> bookmarks) {
        if (bookmarks == null) {
            return null;
        }

        return bookmarks.stream()
            .map(BookmarkMapper::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Converts a PackageNode entity to PackageNodeDto.
     */
    public static PackageNodeDto toDto(PackageNode packageNode) {
        if (packageNode == null) {
            return null;
        }

        List<PackageNodeDto> childrenDtos = packageNode.getChildren() != null 
            ? packageNode.getChildren().stream()
                .map(child -> BookmarkMapper.toDto(child))
                .collect(Collectors.toList())
            : null;

        return new PackageNodeDto(
            packageNode.getKey(),
            childrenDtos
        );
    }

    /**
     * Converts a CategoryNode entity to CategoryNodeDto.
     */
    public static CategoryNodeDto toDto(CategoryNode categoryNode) {
        if (categoryNode == null) {
            return null;
        }

        List<CategoryNodeDto> childrenDtos = categoryNode.getChildren() != null 
            ? categoryNode.getChildren().stream()
                .map(child -> BookmarkMapper.toDto(child))
                .collect(Collectors.toList())
            : null;

        return new CategoryNodeDto(
            categoryNode.getName(),
            childrenDtos,
            categoryNode.getCount()
        );
    }
}
