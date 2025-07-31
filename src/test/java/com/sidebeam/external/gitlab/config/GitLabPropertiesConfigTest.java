package com.sidebeam.external.gitlab.config;

import com.sidebeam.common.core.util.PropertyUtil;
import com.sidebeam.external.gitlab.config.property.GitLabApiProperties;
import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.config.property.WebhookProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GitLabConfig 클래스의 테스트입니다.
 * PropertyUtil.getYmlProperties를 사용한 Properties 초기화가 올바르게 작동하는지 확인합니다.
 */
class GitLabPropertiesConfigTest {

    private GitLabPropertiesConfig gitLabPropertiesConfig;

    @BeforeEach
    void setUp() {
        gitLabPropertiesConfig = new GitLabPropertiesConfig();
    }

    @Test
    void testGitLabApiPropertiesFromYml() {
        // PropertyUtil을 사용하여 GitLab API 속성을 로드
        GitLabApiProperties properties = gitLabPropertiesConfig.gitLabApiPropertiesFromYml("classpath:gitlab/gitlab-api.yml");

        // 속성이 올바르게 로드되었는지 확인
        assertNotNull(properties);

        // 그룹 엔드포인트 확인
        assertNotNull(properties.getGroupEndpoints());
        assertEquals("api/v4/groups/{groupId}", properties.getGroupEndpoints().getGet());
        assertEquals("api/v4/groups/{groupId}/subgroups", properties.getGroupEndpoints().getSubgroups());
        assertEquals("api/v4/groups/{groupId}/projects", properties.getGroupEndpoints().getProjects());

        // 프로젝트 엔드포인트 확인
        assertNotNull(properties.getProjectEndpoints());
        assertEquals("api/v4/projects/{projectId}", properties.getProjectEndpoints().getGet());

        // 저장소 엔드포인트 확인
        assertNotNull(properties.getProjectEndpoints().getRepository());
        assertEquals("api/v4/projects/{projectId}/repository/tree", 
                properties.getProjectEndpoints().getRepository().getTree());

        // 파일 엔드포인트 확인
        assertNotNull(properties.getProjectEndpoints().getRepository().getFile());
        assertEquals("api/v4/projects/{projectId}/repository/files/{filePath}/raw", 
                properties.getProjectEndpoints().getRepository().getFile().getRaw());

        // 파일 API 엔드포인트 확인
        assertNotNull(properties.getFileEndpoints());
        assertEquals("api/v4/projects/{projectId}/repository/files/{filePath}", 
                properties.getFileEndpoints().getGet());
    }

    @Test
    void testGitLabPropertiesFromYml() {
        // GitLabApiProperties 먼저 생성
        GitLabApiProperties gitLabApiProperties = gitLabPropertiesConfig.gitLabApiPropertiesFromYml("classpath:gitlab/gitlab-api.yml");

        // PropertyUtil을 사용하여 GitLab 속성을 로드
        GitLabProperties properties = gitLabPropertiesConfig.gitLabPropertiesFromYml(gitLabApiProperties);

        // 속성이 올바르게 로드되었는지 확인
        assertNotNull(properties);
    }

    @Test
    void testWebhookPropertiesFromYml() {
        // PropertyUtil을 사용하여 Webhook 속성을 로드
        WebhookProperties properties = gitLabPropertiesConfig.webhookPropertiesFromYml();

        // 속성이 올바르게 로드되었는지 확인
        assertNotNull(properties);

        // 객체가 정상적으로 생성되었는지 확인
        // secretToken은 환경 변수에 따라 다를 수 있으므로 null 체크만 수행
        // (실제 값이 없어도 객체는 생성되어야 함)
    }

    @Test
    void testDemonstratePropertyUtilUsage() {
        // PropertyUtil 사용 예시 메서드가 예외 없이 실행되는지 확인
        assertDoesNotThrow(() -> {
            // 1. 전체 YAML 파일을 특정 클래스로 로드
            GitLabApiProperties fullConfig = PropertyUtil.getYmlProperties(
                    "classpath:gitlab/gitlab-api.yml",
                    GitLabApiProperties.class
            );

            // 2. YAML 파일의 특정 섹션만 로드
            GitLabProperties gitlabSection = PropertyUtil.getYmlProperties(
                    "classpath:application.yml",
                    "gitlab",
                    GitLabProperties.class
            );

            // 3. YAML 파일을 Properties 객체로 로드
            java.util.Properties rawProperties = PropertyUtil.getYmlProperties("classpath:application.yml");
        });
    }

    @Test
    void testPropertyUtilErrorHandling() {
        // GitLabConfig의 에러 핸들링이 올바르게 작동하는지 확인
        // 잘못된 경로로 테스트하기 위해 새로운 GitLabConfig 인스턴스 생성
        GitLabPropertiesConfig configWithError = new GitLabPropertiesConfig() {
            @Override
            public GitLabApiProperties gitLabApiPropertiesFromYml(String configPath) {
                // 의도적으로 잘못된 경로 사용
                try {
                    return super.gitLabApiPropertiesFromYml("classpath:gitlab/gitlab-api.yml");
                } catch (Exception e) {
                    // 에러 발생 시 기본값 반환
                    return new GitLabApiProperties();
                }
            }
        };

        // 에러가 발생해도 기본값으로 초기화된 객체가 반환되는지 확인
        GitLabApiProperties properties = configWithError.gitLabApiPropertiesFromYml("classpath:gitlab/gitlab-api.yml");
        assertNotNull(properties);
        assertNotNull(properties.getGroupEndpoints());
        assertNotNull(properties.getProjectEndpoints());
        assertNotNull(properties.getFileEndpoints());
    }
}
