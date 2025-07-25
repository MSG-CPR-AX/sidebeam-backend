package com.sidebeam.bookmark.util;

import com.sidebeam.bookmark.domain.model.PackageNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for building package tree structures.
 * This class contains the business logic previously in PackageNode.
 */
public class PackageTreeBuilder {

    private PackageTreeBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Builds a package tree from a list of package paths.
     *
     * @param packagePaths List of package paths (e.g., "/dev/doc/gitlab")
     * @return The root node of the package tree
     */
    public static PackageNode buildTree(List<String> packagePaths) {
        if (packagePaths == null || packagePaths.isEmpty()) {
            return PackageNode.builder()
                .key("root")
                .children(new ArrayList<>())
                .build();
        }

        // Build tree structure using a map to track nodes
        Map<String, List<PackageNode>> nodeChildren = new HashMap<>();
        nodeChildren.put("root", new ArrayList<>());

        for (String path : packagePaths) {
            if (path == null || path.isEmpty()) {
                continue;
            }

            String[] parts = path.split("/");
            String currentPath = "root";

            // Skip empty first part if path starts with "/"
            int startIndex = (path.startsWith("/") && parts.length > 0) ? 1 : 0;

            for (int i = startIndex; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    final String part = parts[i];
                    String childPath = currentPath + "/" + part;

                    // Initialize children list if not exists
                    nodeChildren.putIfAbsent(currentPath, new ArrayList<>());
                    nodeChildren.putIfAbsent(childPath, new ArrayList<>());

                    // Add child if not already exists
                    List<PackageNode> children = nodeChildren.get(currentPath);
                    boolean exists = children.stream().anyMatch(child -> child.getKey().equals(part));
                    if (!exists) {
                        children.add(PackageNode.builder()
                            .key(part)
                            .children(nodeChildren.get(childPath))
                            .build());
                    }

                    currentPath = childPath;
                }
            }
        }

        return PackageNode.builder()
            .key("root")
            .children(nodeChildren.get("root"))
            .build();
    }
}
