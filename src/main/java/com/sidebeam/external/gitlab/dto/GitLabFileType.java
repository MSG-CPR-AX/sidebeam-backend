package com.sidebeam.external.gitlab.dto;

import lombok.Getter;

/**
 * GitLab Repository Tree API에서 사용되는 파일 타입을 정의하는 열거형
 */
@Getter
public enum GitLabFileType {

    /**
     * 일반 파일
     */
    BLOB("blob"),

    /**
     * 디렉토리
     */
    TREE("tree"),

    /**
     * 커밋 (서브모듈)
     */
    COMMIT("commit");

    private final String value;

    GitLabFileType(String value) {
        this.value = value;
    }

    /**
     * 문자열 값으로부터 GitLabFileType을 찾습니다.
     */
    public static GitLabFileType fromValue(String value) {
        for (GitLabFileType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
