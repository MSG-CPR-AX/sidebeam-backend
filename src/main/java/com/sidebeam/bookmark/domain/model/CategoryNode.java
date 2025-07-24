package com.sidebeam.bookmark.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 북마크 카테고리의 계층 구조를 표현하는 트리 노드 클래스
 * 각 노드는 카테고리명과 하위 카테고리들을 가질 수 있으며, 
 * 재귀적인 트리 구조를 통해 무제한 깊이의 카테고리 계층을 구성할 수 있다.
 * JSON 직렬화 시 빈 필드는 제외되어 클라이언트에게 깔끔한 데이터를 제공한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CategoryNode {

    /**
     * 현재 카테고리 노드의 이름
     * 트리 구조에서 각 노드를 식별하는 핵심 정보로 사용된다.
     */
    private String name;

    /**
     * 현재 노드의 하위 카테고리 목록
     * ArrayList로 초기화되어 동적으로 자식 노드를 추가할 수 있으며,
     * Builder 패턴에서 기본값으로 빈 리스트가 설정된다.
     */
    @Builder.Default
    private List<CategoryNode> children = new ArrayList<>();

    /**
     * 현재 카테고리에 속한 북마크의 개수
     * 하위 카테고리의 북마크는 포함하지 않고 현재 레벨의 북마크만 카운트한다.
     * Builder 패턴에서 기본값 0으로 초기화된다.
     */
    @Builder.Default
    private int count = 0;

    /**
     * 지정된 이름의 자식 노드를 추가하거나 기존 노드를 반환하는 메서드
     * 중복된 카테고리명을 방지하기 위해 기존 자식 노드 중 같은 이름이 있으면 
     * 새로 생성하지 않고 기존 노드를 반환한다. 이를 통해 트리 구조의 일관성을 유지한다.
     */
    public CategoryNode addChild(String childName) {
        // 기존 자식 노드들 중에서 동일한 이름을 가진 노드가 있는지 순차 검색
        for (CategoryNode child : children) {
            if (child.getName().equals(childName)) {
                return child; // 기존 노드 발견 시 즉시 반환하여 중복 생성 방지
            }
        }

        // 동일한 이름의 자식이 없을 경우 새로운 노드 생성
        CategoryNode child = CategoryNode.builder()
                .name(childName)
                .build();
        children.add(child); // 자식 목록에 추가
        return child; // 새로 생성된 노드 반환
    }

    /**
     * 현재 노드의 북마크 카운트를 1 증가시키는 메서드
     * 북마크가 특정 카테고리에 추가될 때마다 호출되어 
     * 해당 카테고리의 북마크 개수를 실시간으로 업데이트한다.
     */
    public void incrementCount() {
        this.count++;
    }

    /**
     * 카테고리 경로 문자열 목록으로부터 전체 카테고리 트리를 구축하는 정적 팩토리 메서드
     * "부모/자식/손자" 형태의 경로 문자열들을 파싱하여 계층적 트리 구조를 생성한다.
     * 루트 노드부터 시작해서 각 경로를 따라 노드들을 생성하거나 기존 노드를 재사용하며,
     * 최종 리프 노드에는 북마크 카운트를 증가시켜 실제 북마크가 위치한 카테고리를 표시한다.
     */
    public static CategoryNode buildTree(List<String> categoryPaths) {
        // 모든 카테고리의 최상위 부모가 되는 루트 노드 생성
        CategoryNode root = CategoryNode.builder()
                .name("root")
                .build();

        // 각 카테고리 경로를 순회하며 트리 구조 구축
        for (String path : categoryPaths) {
            String[] parts = path.split("/"); // 경로를 '/'로 분할하여 각 레벨의 카테고리명 추출
            CategoryNode current = root; // 현재 탐색 중인 노드를 루트부터 시작

            // 경로의 각 부분을 순회하며 트리를 따라 내려가거나 새 노드 생성
            for (String part : parts) {
                current = current.addChild(part); // 자식 노드 추가 또는 기존 노드 반환
            }
            // 경로의 마지막 노드(실제 북마크가 속한 카테고리)의 카운트 증가
            current.incrementCount();
        }

        return root; // 완성된 트리의 루트 노드 반환
    }
}
