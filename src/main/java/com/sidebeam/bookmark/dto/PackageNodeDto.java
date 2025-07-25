package com.sidebeam.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * DTO class for PackageNode data returned by the Controller.
 * This separates the presentation layer from the domain model.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PackageNodeDto(
    String key,
    List<PackageNodeDto> children
) {
}