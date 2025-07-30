package com.sidebeam.bookmark.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sidebeam.bookmark.util.PackageNodeDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * YAML 파일에서 읽어온 북마크 항목을 나타냅니다.
 * 이는 향후 JPA Entity 사용에 적합한 클래스입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bookmark {

    /**
     * 북마크의 이름입니다.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * 북마크의 URL입니다.
     */
    @NotBlank(message = "URL is required")
    private String url;

    /**
     * URL의 도메인입니다.
     */
    @NotBlank(message = "Domain is required")
    private String domain;

    /**
     * "Parent/Child/Grandchild" 형식의 카테고리 경로입니다.
     */
    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^[^/]+(/[^/]+)*$", message = "Category must be in format 'Parent/Child/Grandchild'")
    private String category;

    /**
     * 선택적 패키지 노드 목록입니다.
     */
    @JsonDeserialize(using = PackageNodeDeserializer.class)
    private List<PackageNode> packages;

    /**
     * 키-값 쌍으로 구성된 선택적 메타데이터입니다.
     */
    private Map<String, Object> meta;

    /**
     * 이 북마크가 정의된 소스 파일 경로입니다.
     * 내부적으로 설정되며 YAML의 일부가 아닙니다.
     */
    private String sourcePath;
}