package com.sidebeam.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
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

    /**
     * PackageNodeDto 트리를 경로 문자열 목록으로 변환합니다.
     * 각 문자열은 루트에서 노드까지의 경로를 나타냅니다.
     */
    public List<String> spread(PackageNodeDto root) {
        List<String> paths = new ArrayList<>();
        if (root != null) {
            this.generatePaths(root, "", paths);
        }
        return paths;
    }

    private void generatePaths(PackageNodeDto node, String parentPath, List<String> paths) {
        String currentPath = parentPath.isEmpty() ? node.key() : parentPath + "/" + node.key();
        paths.add(currentPath);

        if (node.children() != null) {
            for (PackageNodeDto child : node.children()) {
                generatePaths(child, currentPath, paths);
            }
        }
    }


}