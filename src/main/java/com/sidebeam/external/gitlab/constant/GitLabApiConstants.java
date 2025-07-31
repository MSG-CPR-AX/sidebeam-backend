package com.sidebeam.external.gitlab.constant;

import lombok.experimental.UtilityClass;

/**
 * GitLab API 요청 시 사용되는 상수들을 정의하는 유틸리티 클래스입니다.
 */
@UtilityClass
public class GitLabApiConstants {
    // Query Parameters
    public static final String PARAM_PER_PAGE = "per_page";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_REF = "ref";
    public static final String PARAM_PATH = "file_path";
    public static final String PARAM_INCLUDE_SUBGROUPS = "include_subgroups";

    // Default Values
    public static final int DEFAULT_PER_PAGE = 100;
}
