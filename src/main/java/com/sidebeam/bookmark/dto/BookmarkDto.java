package com.sidebeam.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * DTO class for Bookmark data returned by the Controller.
 * This separates the presentation layer from the domain model.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookmarkDto(
    String name,
    String url,
    String domain,
    String category,
    List<PackageNodeDto> packages,
    Map<String, Object> meta,
    String sourcePath
) {
}