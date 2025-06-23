package com.sidebeam.bookmark.service;

import java.util.List;
import java.util.Map;

public interface GitLabService {

    /**
     * GitLab 저장소에서 YAML 파일 목록을 가져옵니다.
     * 이 메서드는 GitLab API를 통해 YAML 파일의 경로와 내용을 매핑하는 맵을 반환합니다.
     */
    Map<String, String> fetchAllYamlFiles();

    /**
     * 지정된 파일 경로에 해당하는 YAML 파일의 내용을 가져옵니다.
     */
    String fetchYamlFile(String filePath);

    /**
     * GitLab 리포지토리 내 설정된 경로에서 YAML 파일 목록을 가져옵니다.
     * 이 메서드는 재귀적으로 파일을 탐색하며, 설정된 파일 확장자를 가진 모든 YAML 파일의 경로를 반환합니다.
     */
    List<String> listYamlFiles();
}