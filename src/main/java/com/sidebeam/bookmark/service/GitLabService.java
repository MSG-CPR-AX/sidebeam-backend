package com.sidebeam.bookmark.service;

import com.sidebeam.external.gitlab.dto.AllFilesContentDto;
import java.util.List;

public interface GitLabService {

    /**
     * GitLab 저장소에서 YAML 파일 목록을 가져옵니다.
     * 이 메서드는 GitLab API를 통해 YAML 파일의 경로와 내용을 포함하는 DTO를 반환합니다.
     */
    AllFilesContentDto retrieveAllYamlFiles();
}