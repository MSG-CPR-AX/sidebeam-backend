package com.sidebeam.bookmark.config;

import com.sidebeam.external.gitlab.config.GitLabApiProperties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GitLabApiProperties 클래스의 테스트입니다.
 * gitlab-api.yml 파일에서 속성이 올바르게 로드되는지 확인합니다.
 */
class GitLabApiPropertiesTest {

    private final GitLabApiProperties properties = new GitLabApiProperties();

    @Test
    void testGitLabApiPropertiesLoaded() {
        // PropertyUtil을 사용한 개선된 방식으로 초기화
        try {
            properties.afterPropertiesSet();
        } catch (Exception e) {
            fail("Failed to initialize GitLabApiProperties: " + e.getMessage());
        }

        // 속성이 로드되었는지 확인
        assertNotNull(properties);

        // 그룹 속성 확인
        assertNotNull(properties.getGroupEndpoints());
        assertEquals("api/v4/groups/{groupId}", properties.getGroupEndpoints().getGet());
        assertEquals("api/v4/groups/{groupId}/subgroups", properties.getGroupEndpoints().getSubgroups());
        assertEquals("api/v4/groups/{groupId}/projects", properties.getGroupEndpoints().getProjects());

        // 프로젝트 속성 확인
        assertNotNull(properties.getProjectEndpoints());
        assertEquals("api/v4/projects/{projectId}", properties.getProjectEndpoints().getGet());

        // 저장소 속성 확인
        assertNotNull(properties.getProjectEndpoints().getRepository());
        assertEquals("api/v4/projects/{projectId}/repository/tree", 
                properties.getProjectEndpoints().getRepository().getTree());

        // 파일 속성 확인
        assertNotNull(properties.getProjectEndpoints().getRepository().getFile());
        assertEquals("api/v4/projects/{projectId}/repository/files/{filePath}/raw", 
                properties.getProjectEndpoints().getRepository().getFile().getRaw());

        // 파일 API 속성 확인
        assertNotNull(properties.getFileEndpoints());
        assertEquals("api/v4/projects/{projectId}/repository/files/{filePath}", 
                properties.getFileEndpoints().getGet());
    }
}
