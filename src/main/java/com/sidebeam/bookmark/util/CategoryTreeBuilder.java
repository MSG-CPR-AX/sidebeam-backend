package com.sidebeam.bookmark.util;

import com.sidebeam.bookmark.domain.model.CategoryNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for building category tree structures.
 * This class contains the business logic previously in CategoryNode.
 */
public class CategoryTreeBuilder {

    private CategoryTreeBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * 카테고리 경로 문자열 목록으로부터 전체 카테고리 트리를 구축하는 정적 팩토리 메서드
     * "부모/자식/손자" 형태의 경로 문자열들을 파싱하여 계층적 트리 구조를 생성한다.
     * 루트 노드부터 시작해서 각 경로를 따라 노드들을 생성하거나 기존 노드를 재사용하며,
     * 최종 리프 노드에는 북마크 카운트를 증가시켜 실제 북마크가 위치한 카테고리를 표시한다.
     */
    public static CategoryNode buildTree(List<String> categoryPaths) {
        if (categoryPaths == null || categoryPaths.isEmpty()) {
            return new CategoryNode("root", new ArrayList<>(), 0);
        }

        // Build tree structure using maps to track nodes and counts
        Map<String, List<CategoryNode>> nodeChildren = new HashMap<>();
        Map<String, Integer> nodeCounts = new HashMap<>();

        nodeChildren.put("root", new ArrayList<>());
        nodeCounts.put("root", 0);

        // 각 카테고리 경로를 순회하며 트리 구조 구축
        for (String path : categoryPaths) {
            String[] parts = path.split("/"); // 경로를 '/'로 분할하여 각 레벨의 카테고리명 추출
            String currentPath = "root"; // 현재 탐색 중인 노드 경로를 루트부터 시작

            // 경로의 각 부분을 순회하며 트리를 따라 내려가거나 새 노드 생성
            for (String part : parts) {
                String childPath = currentPath + "/" + part;

                // Initialize children list and count if not exists
                nodeChildren.putIfAbsent(currentPath, new ArrayList<>());
                nodeChildren.putIfAbsent(childPath, new ArrayList<>());
                nodeCounts.putIfAbsent(childPath, 0);

                // Add child if not already exists
                List<CategoryNode> children = nodeChildren.get(currentPath);
                boolean exists = children.stream().anyMatch(child -> child.getName().equals(part));
                if (!exists) {
                    children.add(CategoryNode.builder()
                        .name(part)
                        .children(nodeChildren.get(childPath))
                        .count(nodeCounts.get(childPath))
                        .build());
                }

                currentPath = childPath;
            }
            // 경로의 마지막 노드(실제 북마크가 속한 카테고리)의 카운트 증가
            nodeCounts.put(currentPath, nodeCounts.get(currentPath) + 1);
        }

        // Rebuild tree with updated counts
        return buildNodeWithCounts("root", nodeChildren, nodeCounts);
    }

    private static CategoryNode buildNodeWithCounts(String path, Map<String, List<CategoryNode>> nodeChildren, Map<String, Integer> nodeCounts) {
        String name = path.equals("root") ? "root" : path.substring(path.lastIndexOf("/") + 1);
        List<CategoryNode> children = new ArrayList<>();

        for (CategoryNode child : nodeChildren.get(path)) {
            String childPath = path + "/" + child.getName();
            children.add(buildNodeWithCounts(childPath, nodeChildren, nodeCounts));
        }

        return CategoryNode.builder()
            .name(name)
            .children(children)
            .count(nodeCounts.get(path))
            .build();
    }
}
