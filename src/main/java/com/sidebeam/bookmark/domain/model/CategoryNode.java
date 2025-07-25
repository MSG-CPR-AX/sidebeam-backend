package com.sidebeam.bookmark.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 북마크 카테고리의 계층 구조를 표현하는 트리 노드 클래스
 * 각 노드는 카테고리명과 하위 카테고리들을 가질 수 있으며, 
 * 재귀적인 트리 구조를 통해 무제한 깊이의 카테고리 계층을 구성할 수 있다.
 * JSON 직렬화 시 빈 필드는 제외되어 클라이언트에게 깔끔한 데이터를 제공한다.
 * 이제 JPA Entity로 사용할 수 있는 클래스로 변경되었다.
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
     * 동적으로 자식 노드를 추가할 수 있으며, 재귀적인 트리 구조를 형성한다.
     */
    private List<CategoryNode> children;

    /**
     * 현재 카테고리에 속한 북마크의 개수
     * 하위 카테고리의 북마크는 포함하지 않고 현재 레벨의 북마크만 카운트한다.
     */
    private int count;
}
