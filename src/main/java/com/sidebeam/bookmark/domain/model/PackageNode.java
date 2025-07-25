package com.sidebeam.bookmark.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 패키지 트리의 노드를 나타냅니다.
 * 각 노드는 키를 가지고 있으며 자식 노드들을 가질 수 있습니다.
 * 이는 향후 JPA Entity 사용에 적합한 클래스입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PackageNode {

    /**
     * 이 패키지 노드의 키입니다.
     */
    private String key;

    /**
     * 이 패키지 노드의 자식 노드들입니다.
     */
    private List<PackageNode> children;
}