package com.sidebeam.external.gitlab.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GitLab API 엔드포인트 설정을 위한 구성 클래스입니다.
 * Spring Boot의 @ConfigurationProperties를 사용하여 설정을 자동으로 바인딩합니다.
 * 
 * 이 클래스는 application.yml 또는 별도의 설정 파일에서 gitlab-api 관련 속성을 로드합니다.
 * 복잡한 수동 파싱이나 InitializingBean 구현이 필요하지 않습니다.
 */
@Data
@ConfigurationProperties(prefix = "gitlab-api", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class GitLabApiProperties {

    /**
     * GitLab 그룹 관련 API 엔드포인트 설정
     */
    private GroupEndpoints groups = new GroupEndpoints();

    /**
     * GitLab 프로젝트 관련 API 엔드포인트 설정
     */
    private ProjectEndpoints projects = new ProjectEndpoints();

    /**
     * GitLab 파일 관련 API 엔드포인트 설정
     */
    private FileEndpoints files = new FileEndpoints();

    // Getter methods for backward compatibility
    public GroupEndpoints getGroupEndpoints() {
        return groups;
    }

    public ProjectEndpoints getProjectEndpoints() {
        return projects;
    }

    public FileEndpoints getFileEndpoints() {
        return files;
    }

    /**
     * GitLab 그룹 관련 API 엔드포인트 설정
     */
    @Data
    public static class GroupEndpoints {
        /**
         * 그룹 정보 조회 API 경로
         * GET /api/v4/groups/{groupId}
         */
        private String get = "api/v4/groups/{groupId}";

        /**
         * 하위 그룹 목록 조회 API 경로
         * GET /api/v4/groups/{groupId}/subgroups
         */
        private String subgroups = "api/v4/groups/{groupId}/subgroups";

        /**
         * 그룹 내 프로젝트 목록 조회 API 경로
         * GET /api/v4/groups/{groupId}/projects
         */
        private String projects = "api/v4/groups/{groupId}/projects";
    }

    /**
     * GitLab 프로젝트 관련 API 엔드포인트 설정
     */
    @Data
    public static class ProjectEndpoints {
        /**
         * 프로젝트 정보 조회 API 경로
         * GET /api/v4/projects/{projectId}
         */
        private String get = "api/v4/projects/{projectId}";

        /**
         * 저장소 파일 트리 조회 API 경로
         * GET /api/v4/projects/{projectId}/repository/tree
         */
        private Repository repository = new Repository();

        @Data
        public static class Repository {
            /**
             * 저장소 파일 트리 조회 API 경로
             * GET /api/v4/projects/{projectId}/repository/tree
             */
            private String tree = "api/v4/projects/{projectId}/repository/tree";

            /**
             * 파일 내용 조회 API 경로
             * GET /api/v4/projects/{projectId}/repository/files/{filePath}/raw
             */
            private File file = new File();

            @Data
            public static class File {
                /**
                 * 파일 내용 조회 API 경로
                 * GET /api/v4/projects/{projectId}/repository/files/{filePath}/raw
                 */
                private String raw = "api/v4/projects/{projectId}/repository/files/{filePath}/raw";
            }
        }
    }

    /**
     * GitLab 파일 관련 API 엔드포인트 설정
     */
    @Data
    public static class FileEndpoints {
        /**
         * 파일 정보 조회 API 경로
         * GET /api/v4/projects/{projectId}/repository/files/{filePath}
         */
        private String get = "api/v4/projects/{projectId}/repository/files/{filePath}";
    }
}
