package com.sidebeam.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * DTO class for CategoryNode data returned by the Controller.
 * This separates the presentation layer from the domain model.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CategoryNodeDto(
    String name,
    List<CategoryNodeDto> children,
    int count
) {
}