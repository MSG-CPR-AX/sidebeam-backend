package com.sidebeam.bookmark.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * Represents a bookmark entry from the YAML files.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bookmark {

    /**
     * The name of the bookmark.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * The URL of the bookmark.
     */
    @NotBlank(message = "URL is required")
    private String url;

    /**
     * The domain of the URL.
     */
    @NotBlank(message = "Domain is required")
    private String domain;

    /**
     * The category path in format "Parent/Child/Grandchild".
     */
    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^[^/]+(/[^/]+)*$", message = "Category must be in format 'Parent/Child/Grandchild'")
    private String category;

    /**
     * Optional list of package nodes.
     */
    private List<PackageNode> packages;

    /**
     * Optional metadata as key-value pairs.
     */
    private Map<String, Object> meta;

    /**
     * The source file path where this bookmark was defined.
     * This is set internally and not part of the YAML.
     */
    private String sourcePath;
}
