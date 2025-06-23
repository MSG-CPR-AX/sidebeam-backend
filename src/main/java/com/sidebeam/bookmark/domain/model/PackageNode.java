package com.sidebeam.bookmark.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the package tree.
 * Each node has a key and can have children nodes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PackageNode {

    /**
     * The key of this package node.
     */
    private String key;

    /**
     * The children of this package node.
     */
    @Builder.Default
    private List<PackageNode> children = new ArrayList<>();

    /**
     * Adds a child node with the given key if it doesn't exist already.
     * Returns the child node (either existing or newly created).
     *
     * @param childKey The key of the child node to add
     * @return The child node
     */
    public PackageNode addChild(String childKey) {
        // Check if child already exists
        for (PackageNode child : children) {
            if (child.getKey().equals(childKey)) {
                return child;
            }
        }

        // Create new child
        PackageNode child = PackageNode.builder()
                .key(childKey)
                .build();
        children.add(child);
        return child;
    }

    /**
     * Builds a package tree from a list of package paths.
     *
     * @param packagePaths List of package paths (e.g., "/dev/doc/gitlab")
     * @return The root node of the package tree
     */
    public static PackageNode buildTree(List<String> packagePaths) {
        PackageNode root = PackageNode.builder()
                .key("root")
                .build();

        if (packagePaths == null || packagePaths.isEmpty()) {
            return root;
        }

        for (String path : packagePaths) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            
            String[] parts = path.split("/");
            PackageNode current = root;

            // Skip empty first part if path starts with "/"
            int startIndex = (path.startsWith("/") && parts.length > 0) ? 1 : 0;

            for (int i = startIndex; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    current = current.addChild(parts[i]);
                }
            }
        }

        return root;
    }
}