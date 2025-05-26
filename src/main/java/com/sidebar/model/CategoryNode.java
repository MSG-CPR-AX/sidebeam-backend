package com.sidebar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a node in the category tree.
 * Each node has a name and can have children nodes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CategoryNode {

    /**
     * The name of this category node.
     */
    private String name;

    /**
     * The children of this category node.
     */
    @Builder.Default
    private List<CategoryNode> children = new ArrayList<>();

    /**
     * The count of bookmarks in this category (including subcategories).
     */
    @Builder.Default
    private int count = 0;

    /**
     * Adds a child node with the given name if it doesn't exist already.
     * Returns the child node (either existing or newly created).
     *
     * @param childName The name of the child node to add
     * @return The child node
     */
    public CategoryNode addChild(String childName) {
        // Check if child already exists
        for (CategoryNode child : children) {
            if (child.getName().equals(childName)) {
                return child;
            }
        }

        // Create new child
        CategoryNode child = CategoryNode.builder()
                .name(childName)
                .build();
        children.add(child);
        return child;
    }

    /**
     * Increments the bookmark count for this node.
     */
    public void incrementCount() {
        this.count++;
    }

    /**
     * Builds a category tree from a list of category paths.
     *
     * @param categoryPaths List of category paths (e.g., "Parent/Child/Grandchild")
     * @return The root node of the category tree
     */
    public static CategoryNode buildTree(List<String> categoryPaths) {
        CategoryNode root = CategoryNode.builder()
                .name("root")
                .build();

        for (String path : categoryPaths) {
            String[] parts = path.split("/");
            CategoryNode current = root;

            for (String part : parts) {
                current = current.addChild(part);
            }
            current.incrementCount();
        }

        return root;
    }
}